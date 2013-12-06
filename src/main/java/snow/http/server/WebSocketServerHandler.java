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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import snow.security.Session;
import snow.security.SessionRegistry;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * 
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private WebSocketHandler<WebSocketFrame> handler;
    private WebSocketServerHandshaker hs;

    private Session session;

    public WebSocketServerHandler(WebSocketHandler<WebSocketFrame> handler, WebSocketServerHandshaker hs, Session session) {
        this.handler = handler;
        this.hs = hs;

        this.session = session;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        try (Session _session_ = SessionRegistry._.activate(session)) {

            if (frame instanceof CloseWebSocketFrame) {
                frame.retain();
                handler.close(hs, ctx, (CloseWebSocketFrame) frame);
                return;
            }

            if (frame instanceof PingWebSocketFrame) {
                handler.ping(hs, ctx, (PingWebSocketFrame) frame);
                return;
            }

            handler.handle(hs, ctx, frame);

        } catch (Exception t) {
            throw t;

        } catch (Throwable t) {
            // XXX: is this possible at all?!
            throw (Exception) t;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}