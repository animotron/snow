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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import snow.Core;
import snow.http.auth.Facebook;

import javax.net.ssl.SSLEngine;

//import org.neo4j.graphdb.GraphDatabaseService;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class HttpServer {

    public static final String ROOT = "/";
    public static final String RESOURCE = "/resource/";
    public static final String TMP = "/tmp/";

    private final static int MAX_AGE = 31536000;
    private static final int HTTP_PORT = 5711;

    private static void run(int port) throws Throwable {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new ServerBootstrap().group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new Initializer()).bind(port).sync()
                    .channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            bossGroup.terminationFuture().sync();
            workerGroup.terminationFuture().sync();
        }
    }

    private static void start() throws Throwable {
        // init();
        run(HTTP_PORT);
    }

    private static class Initializer extends ChannelInitializer<SocketChannel> {

        private static HttpHandler[] httpHandlers = {
                new WebSocketUpgradeHttpHandler(),
                //new Facebook(),
                new ApiHttpHandler(),
                new ResourceHttpHandler(RESOURCE, "no-cache"),
                new StaticHttpHandler(TMP, Core.TMP, "private, max-age=" + MAX_AGE),
                new StaticHttpHandler(ROOT, "site", "no-cache"),
        };

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {

            ChannelPipeline pipe = ch.pipeline();

            SSLEngine sslEngine = SSLContextProvider.createSSLEngine();
            if (sslEngine != null) {
                sslEngine.setUseClientMode(false);
                pipe.addLast(new SslHandler(sslEngine));

            }
            pipe.addLast(new HttpRequestDecoder(), new HttpObjectAggregator(65536), // 1048576
                    new HttpResponseEncoder(), new ChunkedWriteHandler(),
                    // new HttpContentCompressor(),
                    new HttpServerHandler(httpHandlers));
        }
    }

    public static void main(String[] args) {
        try {
            // initialize
//            GraphDatabaseService DB = Core.DB;
//            Primitives.init();
            
//            TTY load = new TTY();
//            PILE pile = new PILE();

            start();

            // Shell.process();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
