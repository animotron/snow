/*
 * Copyright (c) 2013 Public domain
 * http://animotron.org/snow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package snow.http.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import javolution.util.FastMap;
import snow.Term;
import snow.http.server.HttpErrorHelper;
import snow.http.server.HttpHandler;
import snow.security.Session;
import snow.security.SessionRegistry;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static snow.http.server.HttpHelper.sendRedirect;
import static snow.security.SessionRegistry.SUBJECT;

//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Transaction;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class Facebook implements HttpHandler {

    public static final String FACEBOOK_ID = "facebook:id";

    private static final String ROOT = "/auth/";

    private static final String AppId;
    private static final String AppSecret;

    private static final String REDIRECT_URI;

    static {
        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream("etc/facebook.conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        AppId = prop.getProperty("AppID");
        AppSecret = prop.getProperty("AppSecret");
        REDIRECT_URI = prop.getProperty("RedirectUri");
    }

    private static final String scope = "email";

    private static final String STATE = "state";

    private static final String ACCESS_TOKEN = "facebook:accessToken";
    private static final String EXPIRES_AT = "facebook:expires_at";

    // XXX: $appsecret_proof= hash_hmac('sha256', $access_token, $app_secret);
    // XXX: state (An arbitrary unique string created by your app to guard
    // against Cross-site Request Forgery)
    private static final String AUTHORIZE =
    // "https://www.facebook.com/dialog/oauth"
    "https://graph.facebook.com/oauth/authorize" + "?client_id=" + AppId + "&redirect_uri=" + REDIRECT_URI + "&response_type=code" + "&scope=" + scope
            + "&state=";

    public static String AUTH = "https://graph.facebook.com/oauth/access_token" + "?client_id=" + AppId + "&redirect_uri=" + REDIRECT_URI + "&client_secret="
            + AppSecret + "&code=";

    private final static String[] FIELDS = { "id", "name", "first_name", "middle_name", "last_name", "gender", "languages", "verified", "email", "picture"
    };

    public static String PROFILE = "https://graph.facebook.com/me" + "?fields=" + join(FIELDS, ",") + "&access_token=";

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        String uri = request.getUri();
        if (!uri.startsWith(ROOT)) return false;

        if (!request.getMethod().equals(GET)) {
            HttpErrorHelper.handle(ctx, request, METHOD_NOT_ALLOWED);
            return true;
        }

        // System.out.println(uri);

        String function = uri.substring(ROOT.length());

        if (function.startsWith("facebook/")) {

            Session session = SessionRegistry._.active();

            if (session != null) {
                Long expiresAt = session.getLong(EXPIRES_AT);
                if (expiresAt != null) {
                    if (expiresAt > System.currentTimeMillis()) {
                        sendRedirect(ctx, request, "/");
                        return true;
                    } else {
                        session.remove(ACCESS_TOKEN);
                        session.remove(EXPIRES_AT);
                        session.remove(SUBJECT);
                    }
                }
            }

            FastMap<String, String> map = new FastMap<String, String>();
            try {

                parseParams(map, function);

                if (map.containsKey("error")) {
                    HttpErrorHelper.handle(ctx, request, FORBIDDEN);
                    return true;

                } else if (map.containsKey("error_code")) {
                    HttpErrorHelper.handle(ctx, request, FORBIDDEN);
                    return true;

                } else if (map.containsKey("code")) {

                    if (map.containsKey(STATE)) {
                        if (session != null && !map.get(STATE).equals(session.ID()._)) {
                            HttpErrorHelper.handle(ctx, request, UNAUTHORIZED);
                            return true;
                        } else {
                            session = SessionRegistry._.activate(map.get(STATE));

                            // check 'state'
                            if (session == null || !map.get(STATE).equals(session.ID()._)) {
                                HttpErrorHelper.handle(ctx, request, UNAUTHORIZED);
                                return true;
                            }
                        }
                    } else {
                        HttpErrorHelper.handle(ctx, request, UNAUTHORIZED);
                        return true;
                    }

                    URL url = new URL(AUTH + map.get("code").trim());

                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                    try (InputStream stream = con.getInputStream()) {

                        if (con.getResponseCode() == 200) {

                            readAccessToken(stream);
                        }

                    } catch (IOException e) {
                        HttpErrorHelper.handle(ctx, request, FORBIDDEN);
                        return true;
                    }

                    sendRedirect(ctx, request, "/");
                    return true;
                }

                if (session == null) session = SessionRegistry._.make();

                sendRedirect(ctx, request, AUTHORIZE + session.ID()._);
                return true;

            } catch (Throwable e) {
                e.printStackTrace();
                HttpErrorHelper.handle(ctx, request, INTERNAL_SERVER_ERROR);
                return true;
            }

        } else {
            HttpErrorHelper.handle(ctx, request, NOT_FOUND);
            return true;
        }
    }

    private void readAccessToken(InputStream is) throws IOException {
        StringBuilder in = new StringBuilder(512);

        try {
            int i = 0;
            int r;

            while ((r = is.read()) != -1) {
                in.append((char) r);

                if (i++ > 10240) throw new IOException("too big response for access_token");
            }
        } finally {
            is.close();
        }

        String accessToken = null;
        int expires = -1;

        String responce = in.toString();
        String[] pairs = responce.split("&");

        if (pairs.length != 2) throw new IOException("wrong responce for access_token: number of parameters is " + pairs.length);

        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length != 2) {
                throw new IOException("wrong responce for access_token: pair '" + pair + "'");
            } else {
                if ("access_token".equals(kv[0])) {
                    accessToken = kv[1];
                } else if ("expires".equals(kv[0])) {
                    try {
                        expires = Integer.valueOf(kv[1]);
                    } catch (NumberFormatException e) {
                        throw new IOException("wrong responce for access_token: invalid expires '" + kv[1] + "'");
                    }
                }
            }
        }

        if (accessToken == null) throw new IOException("no access token");

        if (expires <= 0) throw new IOException("access token expired");

        makePersistent(accessToken, expires);
    }

    private void makePersistent(String accessToken, int expires) {

        Term person = fetchProfile(accessToken);

        // System.out.println("person = "+person.debug(0));

        if (person != null) {
            Session session = SessionRegistry._.active();
            if (session == null) session = SessionRegistry._.make();

            session.put(SUBJECT, person);

            session.put(ACCESS_TOKEN, accessToken);
            session.put(EXPIRES_AT, System.currentTimeMillis() + expires);
        }

        // System.out.println("accessToken = "+accessToken);
        // System.out.println("expires = "+expires);

    }

    private void parseParams(Map<String, String> map, String str) {
        int pos = str.indexOf('?');
        if (pos >= 0) {
            String query = str.substring(pos + 1);

            String[] pairs = query.split("&");

            for (int i = 0; i < pairs.length; i++) {
                String[] pair = pairs[i].split("=");

                if (pair.length == 2) {
                    map.put(pair[0], pair[1]);
                }
            }
        }
    }

    private Term fetchProfile(String accessToken) {
        try {

            URL url = new URL(PROFILE + accessToken);// +"&appsecret_proof="+MDigest.HmacSHA256(AppSecret,
                                                     // accessToken));

            // System.out.println(url);

            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            try (InputStream stream = con.getInputStream()) {

                if (con.getResponseCode() == 200) {

                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode root = mapper.readTree(url.openStream());

                    JsonNode node = root.get("id");
                    if (node != null) {
                        String fbID = node.asText();

//                        try (Transaction tx = Core.DB.beginTx()) {
//                            TermMutable term;
//                            Node termNode = Core.search(FACEBOOK_ID, fbID);
//                            if (termNode == null) {
//                                term = Unstable.term();
//
//                                term.property(FACEBOOK_ID, fbID);
//
//                                term.type(Primitives.PERSON);
//                            } else {
//                                term = TermInDB.term(termNode);
//                            }
//
//                            term.link(new URL("https://www.facebook.com/profile.php?id=" + fbID));
//
//                            for (String field : FIELDS) {
//                                node = root.get(field);
//
//                                if (node != null) {
//                                    switch (field) {
//                                    case "languages":
//                                        // XXX: code
//                                        break;
//
//                                    case "picture":
//                                        JsonNode data = node.get("data");
//                                        if (data != null) {
//                                            JsonNode picURL = data.get("url");
//                                            if (picURL != null) {
//                                                try {
//                                                    URL imgURL = new URL(picURL.asText());
//
//                                                    term.property("facebook:" + field, imgURL.toString());
//
//                                                    term.visual(imgURL);
//                                                } catch (MalformedURLException e) {
//                                                }
//                                            }
//                                        }
//
//                                        break;
//
//                                    case "name":
//                                        term.label(ORIGINAL_LANGUAGE, node.asText());
//
//                                    default:
//                                        term.property("facebook:" + field, node.asText());
//
//                                        break;
//                                    }
//                                }
//                            }
//
//                            // System.out.println("term = "+term.debug(0));
//
//                            TermInDB person = TermInDB.term(term);
//
//                            tx.success();
//
//                            return person;
//                        }
                    }
                }
            } catch (IOException e) {
                // e.printStackTrace();
                //
                // System.out.println("response code = "+con.getResponseCode());
                //
                // try (InputStream stream = con.getErrorStream()) {
                // java.util.Scanner s = new
                // java.util.Scanner(stream).useDelimiter("\\A");
                // System.out.println(s.hasNext() ? s.next() : "");
                // }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String join(String[] items, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for (String s : items) {

            sb.append(loopDelim);
            sb.append(s);

            loopDelim = delim;
        }

        return sb.toString();
    }
}
