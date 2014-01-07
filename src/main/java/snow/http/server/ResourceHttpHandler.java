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
package snow.http.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import org.apache.commons.io.IOUtils;

import javax.rmi.CORBA.Util;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.getHeader;
import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static javax.activation.FileTypeMap.getDefaultFileTypeMap;
import static snow.http.server.HttpHelper.sendHttpResponse;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class ResourceHttpHandler implements HttpHandler {

    private final String uriContext;
    private final String cache;

    public ResourceHttpHandler(String uriContext, String cache){
        this.uriContext = uriContext;
        this.cache = cache;
    }

    public static String mimeType(Path path) throws IOException {
        String mime = Files.probeContentType(path);
        if (mime == null) mime = getDefaultFileTypeMap().getContentType(path.toString());
        return mime;
    }

    private final static boolean useSendFile = true;

    private void sendURL(final ChannelHandlerContext ctx, final FullHttpRequest request, final String uri, final InputStream is) throws Throwable {
        //final RandomAccessFile raf = new RandomAccessFile(file, "r");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        //long modified = file.lastModified();
        Date since = HttpHelper.parseDate(getHeader(request, IF_MODIFIED_SINCE));
//        if (since != null && since.getTime() >= modified) {
//            HttpHelper.sendStatus(ctx, NOT_MODIFIED);
//        } else {
            byte[] buff = IOUtils.toByteArray(is);
            final long length = buff.length;
            HttpHelper.setDate(response);
//            HttpHelper.setLastModified(response, modified);
            HttpHeaders.setContentLength(response, length);
            HttpHeaders.setHeader(response, CONTENT_TYPE, mimeType((new File(uri)).toPath()));
            setHeader(response, CACHE_CONTROL, cache);

//            boolean isKeep = isKeepAlive(request);
//            if (isKeep) {
//                setHeader(response, CONNECTION, KEEP_ALIVE);
//            }

//            ChannelFuture writeFuture = ctx.writeAndFlush(new ChunkedFile(raf, 0, length, 8192));



            response.content().writeBytes(buff);

            sendHttpResponse(ctx, request, response);


//            }
//        }
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        String uri = request.getUri();
        if (!uri.startsWith(uriContext)) return false;

        if (!request.getMethod().equals(GET)) {
            HttpErrorHelper.handle(ctx, request, METHOD_NOT_ALLOWED);
            return true;
        }

        uri = uri.substring(uriContext.length() - 1);

        InputStream is = getClass().getResourceAsStream(uri);

        try {
            if (is == null) {
                HttpErrorHelper.handle(ctx, request, NOT_FOUND);
                return true;
            }
            //XXX: check that absolute URL starts from SITE
            sendURL(ctx, request, uri, is);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }

        return true;
    }
}
