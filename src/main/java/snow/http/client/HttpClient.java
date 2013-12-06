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
package snow.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.GZIP;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * 
 */
public class HttpClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 80;

    private final int port;
    private final String host;
    private final ChannelHandler handler;
    private Channel ch = null;

    public HttpClient(ChannelHandler handler) {
        this(DEFAULT_HOST, DEFAULT_PORT, handler);
    }

    public HttpClient(int port, ChannelHandler handler) {
        this(DEFAULT_HOST, port, handler);
    }

    public HttpClient(String host, ChannelHandler handler) {
        this(host, DEFAULT_PORT, handler);
    }

    public HttpClient(String host, int port, ChannelHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public HttpClient connect() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new HttpClientInitializer(handler));
            ch = b.connect(host, port).sync().channel();
        } catch (InterruptedException e) {
            disconnect();
        }
        return this;
    }

    public HttpClient disconnect() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            group.shutdownGracefully();
        }
        return this;
    }

    public HttpClient get(String uri) {
        if (ch == null) connect();
        HttpRequest request = new DefaultHttpRequest(HTTP_1_1, GET, uri);
        setHeader(request, HOST, host);
        setHeader(request, CONNECTION, KEEP_ALIVE);
        setHeader(request, ACCEPT_ENCODING, GZIP);
        ch.writeAndFlush(request);
        return this;
    }

}
