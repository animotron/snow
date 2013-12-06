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
package snow.security;

import javolution.util.FastMap;
import snow.ID;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class Session implements AutoCloseable {

    private final ID ident = ID.random();

    private long lastAccessTime = System.currentTimeMillis();

    private FastMap<String, Object> props = new FastMap<String, Object>();

    protected Session() {}

    public ID ID() {
        return ident;
    };

    protected void touch() {
        lastAccessTime = System.currentTimeMillis();
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void put(String key, Object value) {
        props.put(key, value);
    }

    public Object get(String key) {
        return props.get(key);
    }

    public Integer getInteger(String key) {
        return (Integer) props.get(key);
    }

    public Long getLong(String key) {
        return (Long) props.get(key);
    }

    public void remove(String key) {
        props.remove(key);
    }

    @Override
    public void close() throws Exception {
        SessionRegistry._.deactivation(this);

        touch();
    }

    public boolean isAuthorized() {
        return props.containsKey(SessionRegistry.SUBJECT);
    }
}
