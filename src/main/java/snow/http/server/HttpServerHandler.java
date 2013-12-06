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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import snow.security.Session;
import snow.security.SessionRegistry;

import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static snow.security.SessionRegistry.SESSION_COOKIE;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static HttpHandler[] handlers;

    public HttpServerHandler(HttpHandler[] handlers) {
        HttpServerHandler.handlers = handlers;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {

        System.out.println(request.getUri() + " " +Thread.currentThread());
        
        try (Session session = restorySession(request)) {
            if (request.getDecoderResult().isSuccess()) {
                for (HttpHandler handler : handlers)
                    if (handler.handle(ctx, request))
                        return;
                HttpErrorHelper.handle(ctx, request, NOT_FOUND);
            }
            HttpErrorHelper.handle(ctx, request, BAD_REQUEST);
        } catch (Throwable t) {
            HttpErrorHelper.handle(ctx, request, t);
        }

    }

    private Session restorySession(FullHttpRequest request) {
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
