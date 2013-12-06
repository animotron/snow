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

import com.fasterxml.jackson.core.io.CharTypes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import javolution.util.FastMap;
import snow.ID;
import snow.http.server.ApiWebSocketHandler;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Terminal {
    
    private static final FastMap<String, Terminal> terminals = new FastMap<String, Terminal>();
    
    public static Terminal create() {
        Terminal terminal = new Terminal();
        
        terminals.put(terminal.id(), terminal);
        
        return terminal;
    }

    public static Terminal get(String id) {
        return terminals.get(id);
    }

    protected static AtomicLong lastCommandId = new AtomicLong(0);
    
    protected static Map<Long, Command> commands = new FastMap<Long, Command>();

    private final ID id = ID.random();
    
    private ChannelHandlerContext ctx;
    
    private Command activeCommand;
    
    private Terminal() {
        ctx = ApiWebSocketHandler.ctx();
        
        activeCommand = new Shell(this);
    }
    
    public String id() {
        return id._;
    }
    
    public static final String CRLF = "\n";
    
    public void in(String data) {
        activeCommand.stdin(data);
    }

    public void out(String data) {
        
        if (data == null || data.isEmpty())
            return;
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("{\"$stdout\": {\"id\": \"").append(id());
        //appendQuoted(sb, id());
        
        sb.append("\", \"data\": \"");
        CharTypes.appendQuoted(sb, data);
        
        sb.append("\"}}");

        //System.out.println(sb.toString());
        
        try {
            ctx.writeAndFlush(new TextWebSocketFrame(sb.toString()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    

    public void println(Object data) {
        out(data.toString() + CRLF);
    }
    
    public void shutdown() {
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }
    
    static TreeMap<String, Class<? extends Command>> avalableCommands = new TreeMap<String, Class<? extends Command>>();
    
    static {
        //avalableCommands.put("wiki", WikiCommand.class);
    }
    
    ExecutorService executor = Executors.newFixedThreadPool(5);

    
    class Shell extends CommandAbstract {
        
        Worker worker = null;
        
        StringBuilder sb = new StringBuilder();

        public Shell(Terminal terminal) {
            super(terminal);
            
            StringBuilder sb = new StringBuilder();
            terminal.out("Welcome!\r\n");
            prompt(sb);
            terminal.out(sb.toString());
        }
        
        public void stdin(String data) {
            StringBuilder cur = new StringBuilder();
            
            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);
                
                if (c == 0x7F) {
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        cur.append("\b").append((char)27).append("[P");
                    }

                } else if (c == '\t') {
                    String searching = sb.toString();
                    NavigableMap<String, Class<? extends Command>> prossibleCommands = avalableCommands.tailMap(searching, true);
                    
                    if (prossibleCommands.size() == 0) {
                        
                    } else if (prossibleCommands.size() == 1) {
                        
                        String str = prossibleCommands.firstKey();
                        
                        str = str.substring(searching.length());
                        
                        sb.append(str);
                        cur.append(str);

                    } else {
                        StringBuilder tmp = new StringBuilder();
                        tmp.append(CRLF);
                        for (String str : prossibleCommands.keySet()) {
                            tmp.append(str).append(CRLF);
                        }
                        
                        prompt(tmp);
                        
                        tmp.append(sb.toString());
                        terminal.out(tmp.toString());
                        return;
                    }
                    
                } else if (c == '\r') {
                    cur.append(CRLF);
                    cur.append(sb);
                    cur.append(CRLF);
                    terminal.out(cur.toString());
                    
                    String tmp = sb.toString();
                    
                    Class<? extends Command> commandClass = avalableCommands.get(tmp);
                    
                    Thread thread = null;
                    try {
                        Constructor<? extends Command> constructor = commandClass.getConstructor(Terminal.class);
                        Command command = constructor.newInstance(Terminal.this);
                        
                        Worker worker = new Worker(command, this);
                        
                        terminal.activeCommand = command;
                        
                        executor.execute(worker);

                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    if (thread == null) {
                        prompt();
                    }
                    sb = new StringBuilder();
                    cur = new StringBuilder();
                    
                    return;
                    
                } else if (c == '\n') {
                    
                } else {
                    sb.append(c);
                    cur.append(c);
                }
            }
            terminal.out(cur.toString());
        }
        
        public void prompt() {
            terminal.out("$ ");
        }

        public void prompt(StringBuilder sb) {
            sb.append("$ ");
        }

        @Override
        public void process() {
            prompt();
        }
    }
    
    class Worker implements Runnable {

        Command _command;
        Shell _shell;
        
        public Worker(Command command, Shell shell) {
            _command = command;
            _shell = shell;
        }

        @Override
        public void run() {
            try {
                _command.process();
            } catch (Throwable e) {
                //XXX: send to out
            }
            Terminal.this.activeCommand = _shell;
            _shell.process();
            
         }
    }
}
