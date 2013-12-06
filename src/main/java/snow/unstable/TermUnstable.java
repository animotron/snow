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
package snow.unstable;

import javolution.util.FastMap;
import snow.ID;
import snow.TermMutable;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class TermUnstable implements TermMutable {

    private ID type = null;
    private FastMap<ID, String> labels = new FastMap<ID, String>();
    private URL link = null;
    private String picture = null;

    Map<String, String> properties = new HashMap<String, String>();

    public TermUnstable() {}

    public ID id() {
        throw new RuntimeException("unsupported operation");
    }

    @Override
    public ID type() {
        return type;
    }

    public void type(ID type) {
        this.type = type;
    }

    @Override
    public String label() {
        if (labels.isEmpty()) return null;

        return labels.values().iterator().next();
    }

    @Override
    public String label(ID language) {
        return labels.get(language);
    }

    // @Override
    public Map<ID, String> labels() {
        return labels.unmodifiable();
    }

    public void label(ID language, String name) {
        labels.put(language, name);
    }

    @Override
    public String link() {
        return link.toString();
    }

    public void link(URL uri) {
        link = uri;
    }

    @Override
    public String visual() {
        return picture;
    }

    public void visual(URL uri) {
        picture = uri.toString();
    }

    // @Override
    // public Map<String, String> properties() {
    // return properties;
    // }

    public void property(String key, String value) {
        properties.put(key, value);
    }

    public String property(String key) {
        return properties.get(key);
    }

    public Set<String> propertiesKey() {
        return properties.keySet();
    }

    public String toString() {
        return debug(0);
    }

    public String debug(int offset) {

        char[] ws = new char[offset];
        Arrays.fill(ws, ' ');

        StringBuilder sb = new StringBuilder();

//        sb.append(ws).append("TERM unstable [").append(Primitives.label(type()._)).append("] \n")
//
//        .append(ws).append(" type = ").append(Primitives.label(type)).append("\n").append(ws).append(" link = ").append(link).append("\n").append(ws)
//                .append(" visual = ").append(picture).append("\n");
//
//        for (Entry<String, String> entry : properties.entrySet()) {
//            sb.append(ws).append(" ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
//        }
//
//        sb.append(ws).append(" labels:\n");
//        for (Entry<ID, String> entry : labels().entrySet()) {
//
//            sb.append(ws).append(" ['").append(Primitives.label(entry.getKey()._)).append("' : '").append(entry.getValue()).append("']\n");
//        }

        return sb.toString();
    }
}
