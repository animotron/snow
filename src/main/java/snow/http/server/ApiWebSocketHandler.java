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

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import javolution.util.FastMap;
import sebebe.Function;
import sebebe.Manager;

import java.io.IOException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * 
 */

public class ApiWebSocketHandler extends snow.http.server.WebSocketHandler<TextWebSocketFrame> {
    
    private static final FastMap<String, Manager> protocols = new FastMap<String, Manager>();

    public static synchronized void register(String protocol, Function function) {
        Manager api = protocols.get(protocol);
        if (api == null) {
            api = new Manager(ApiWebSocketHandler.class.getName());
            protocols.put(protocol, api);
            WebSocketUpgradeHttpHandler.handlers.put(protocol, (WebSocketHandler) new ApiWebSocketHandler(protocol));
        }

        api.register(function);
    }
    
    private static final FastMap<Thread, ChannelHandlerContext> ctxs = new FastMap<Thread, ChannelHandlerContext>();

    public static ChannelHandlerContext ctx() {
        return ctxs.get(Thread.currentThread());
    }
    
    private Manager api;

    public ApiWebSocketHandler(String protocol) {
        super(protocol);

        api = protocols.get(protocol);

        assert api != null;
    }

    @Override
    public void handle(WebSocketServerHandshaker hs, final ChannelHandlerContext ctx, TextWebSocketFrame frame) throws IOException {
        ctxs.put(Thread.currentThread(), ctx);
        try {
            JsonNode result = api.execute(frame.text());
            if (result != null) {
                ctx.writeAndFlush(new TextWebSocketFrame(result.toString()));
            }
        } finally {
            ctxs.remove(Thread.currentThread());
        }
    }

}