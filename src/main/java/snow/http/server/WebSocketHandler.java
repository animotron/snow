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
import io.netty.handler.codec.http.websocketx.*;

import java.io.IOException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */

public abstract class WebSocketHandler<T extends WebSocketFrame>{

    public final String protocol;

    public WebSocketHandler () {
        this(null);
    }

    public WebSocketHandler (String protocol) {
        this.protocol = protocol;
    }

    public abstract void handle(WebSocketServerHandshaker hs, ChannelHandlerContext ctx, T frame) throws IOException;

    public void open(WebSocketServerHandshaker hs, ChannelHandlerContext ctx) {}

    public void ping(WebSocketServerHandshaker hs, ChannelHandlerContext ctx, PingWebSocketFrame frame) {
        ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
    }

    public void close(WebSocketServerHandshaker hs, ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        hs.close(ctx.channel(), frame.retain());
    }

}