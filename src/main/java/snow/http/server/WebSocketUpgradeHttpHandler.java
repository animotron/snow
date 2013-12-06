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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import javolution.util.FastMap;
import snow.security.Session;
import snow.security.SessionRegistry;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.getHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.UPGRADE_REQUIRED;
import static io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse;
import static snow.http.server.HttpHelper.sendStatus;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * 
 */
public class WebSocketUpgradeHttpHandler implements HttpHandler {

    protected static final String uriContext = "/ws";

    protected static FastMap<String, WebSocketHandler<WebSocketFrame>> handlers = new FastMap<String, WebSocketHandler<WebSocketFrame>>();

    public WebSocketUpgradeHttpHandler() {}

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Throwable {
        if (!request.getUri().equals(uriContext)) return false;

        if (!request.getMethod().equals(GET)) {
            HttpErrorHelper.handle(ctx, request, METHOD_NOT_ALLOWED);
            return true;
        }

        try (Session session = SessionRegistry._.restorySession(request)) {

//            if (session == null || !session.isAuthorized()) {
//                HttpErrorHelper.handle(ctx, request, FORBIDDEN);
//                return true;
//            }

            if (!"websocket".equals(getHeader(request, UPGRADE))) {
                sendStatus(ctx, UPGRADE_REQUIRED);
                return true;
            }

            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), getProtocol(request), false);

            WebSocketServerHandshaker hs = wsFactory.newHandshaker(request);
            if (hs == null) {
                sendUnsupportedWebSocketVersionResponse(ctx.channel());
                return true;
            }

            WebSocketHandler<WebSocketFrame> handler = selectHandler(getProtocol(request));
            if (handler == null) {
                hs.handshake(ctx.channel(), request);
                hs.close(ctx.channel(), new CloseWebSocketFrame());
                return true;
            }

            handler.open(hs, ctx);
            ctx.pipeline().removeLast();
            ctx.pipeline().addLast(new WebSocketServerHandler(handler, hs, session));
            hs.handshake(ctx.channel(), request);

            return true;
        }
    }

    private WebSocketHandler<WebSocketFrame> selectHandler(String protocol) {
        return handlers.get(protocol);
    }

    private String getProtocol(FullHttpRequest request) {
        return getHeader(request, SEC_WEBSOCKET_PROTOCOL);
    }

    private String getWebSocketLocation(FullHttpRequest request) {
        return "ws://" + getHeader(request, HOST) + request.getUri();
    }

}
