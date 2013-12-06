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
package snow.utils;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class Pipe<T> extends SynchronousQueue<T> implements AutoCloseable {

    private static final long serialVersionUID = 1979019830711126462L;

    private final static Object EOP = new Object();

    public static Pipe newInstance() {
        return new Pipe();
    }

    public static void recycle(Pipe instance) {}

    public void write(T o) throws IOException {
        try {
            put(o);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public void close() {
        try {
            put((T) EOP);
        } catch (InterruptedException e) {
            // XXX: log
            e.printStackTrace();
        }
    }

    @Override
    public T take() {
        try {
            T o = super.take();

            if (o == EOP) return null;

            return o;
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void reset() {
        clear();
    }
}
