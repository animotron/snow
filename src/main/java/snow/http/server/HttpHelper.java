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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import snow.security.SessionRegistry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class HttpHelper {

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    private static ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

            return dateFormatter;
        }
    };

    protected static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse response) {
        setContentLength(response);

        boolean keepAlive = isKeepAlive(req);

        if (!keepAlive) {
            ctx.channel().write(response).addListener(CLOSE);
        } else {
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            ctx.channel().write(response);
        }
    }

    public static void sendStatus(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        setContentLength(response);
        setDate(response);
        ctx.write(response).addListener(CLOSE);
    }

    public static void setDate(FullHttpResponse response) {
        setDateHeader(response, DATE, System.currentTimeMillis());
    }

    public static void setLastModified(FullHttpResponse response, long time) {
        setDateHeader(response, LAST_MODIFIED, time);
    }

    private static void setDateHeader(FullHttpResponse response, String header, long time) {
        setHeader(response, header, formatDate(time));
    }

    private static void setContentLength(FullHttpResponse response) {
        HttpHeaders.setContentLength(response, response.content().readableBytes());
    }

    protected static String formatDate(long time) {
        return df.get().format(new Date(time));
    }

    protected static Date parseDate(String time) {
        if (time == null) return null;
        if (time.isEmpty()) return null;
        try {
            return df.get().parse(time);
        } catch (ParseException | NumberFormatException e) {
            return null;
        }
    }

    public static void sendRedirect(ChannelHandlerContext ctx, FullHttpRequest request, String newUri) {
        FullHttpResponse response = newResponce(request, FOUND);
        response.headers().set(LOCATION, newUri);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public static FullHttpResponse newResponce(FullHttpRequest request, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);

        SessionRegistry._.checkCookie(request, response);

        return response;
    }

}
