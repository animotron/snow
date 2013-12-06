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
package snow.console;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sebebe.Function;

import java.util.Map;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Stdin extends Function {

    public Stdin() {
        super("stdin");
    }

    @Override
    public Map<String, JsonNode> execute(JsonNode params) {
//        System.out.println(params.toString());
        
        if (params instanceof ObjectNode) {
            ObjectNode node = (ObjectNode) params;
            
            JsonNode id = node.get("id");
            if (id != null) {
                JsonNode data = node.get("data");
                
                if (data != null) {
                    Terminal terminal = Terminal.get(id.asText());
                    
                    if (terminal != null) {
                        terminal.in(data.asText());
                    }
                }
            }
        }
        
        return null;
    }

}
