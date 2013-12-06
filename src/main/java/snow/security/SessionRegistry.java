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
package snow.security;

import io.netty.handler.codec.http.*;
import javolution.util.FastMap;
import snow.ID;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class SessionRegistry {

    public static final SessionRegistry _ = new SessionRegistry();

    public static final String SESSION_COOKIE = "MEM_SESSION";

    public static final String SUBJECT = "mymem:subject";

    protected final Map<UUID, Session> sessions;
    protected final Map<Thread, Session> threads;

    private SessionRegistry() {
        sessions = new FastMap<UUID, Session>().shared();
        threads = new FastMap<Thread, Session>().shared();
    }

    public Session get(ID key) {
        return get(key.uuid());
    }

    public Session get(UUID key) {
        Session session = sessions.get(key);

        if (session == null) return null;

        session.touch();

        return session;
    }

    public Session make() {

        Session session = new Session();
        sessions.put(session.ID().uuid(), session);

        threads.put(Thread.currentThread(), session);

        return session;
    }

    public boolean remove(UUID id) {
        return sessions.remove(id) != null ? true : false;
    }

    public Session active() {
        return threads.get(Thread.currentThread());
    }

    public Session activate(String id) {
        Session session = null;

        if (id != null && id.length() == 36) {
            try {
                session = get(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
            }
        }

        return activate(session);
    }

    public Session activate(Session session) {
        if (session != null) {
            threads.put(Thread.currentThread(), session);
        }

        return session;
    }

    public void deactivation(Session session) {
        if (session != null) {
            // XXX: check is it same session or not?
            threads.remove(Thread.currentThread());
        }
    }

    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    public String newAccessToken() {

        SecureRandom ng = Holder.numberGenerator;

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);

        return new String(encodeBase64URLSafeString(randomBytes));
    }

    public Session restorySession(FullHttpRequest request) {
        String cookieString = request.headers().get(COOKIE);

        String id = null;

        if (cookieString != null) {

            Set<Cookie> decoded = CookieDecoder.decode(cookieString);

            for (Cookie cookie : decoded) {
                if (SESSION_COOKIE.equals(cookie.getName())) {
                    id = cookie.getValue();
                    break;
                }
            }
        }

        return SessionRegistry._.activate(id);
    }

    public void checkCookie(FullHttpRequest request, FullHttpResponse response) {

        Session session = active();

        if (session != null) {
            DefaultCookie cookie = new DefaultCookie(SESSION_COOKIE, session.ID().toString());
            cookie.setMaxAge(60 * 60 * 24 * 14); // 1h * 24 * 14 (2 weeks)

            response.headers().set(SET_COOKIE, ServerCookieEncoder.encode(cookie));
        }
    }
}
