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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class SSLContextProvider {

    private static final char[] secret = "secret".toCharArray();

    private static SSLContext sslContext = null;

    private static SSLContext get() {
        if (sslContext == null) {
            synchronized (SSLContextProvider.class) {
                if (sslContext == null) {
                    try {
                        File p12 = new File("etc/site.p12");
                        if (p12.exists() && p12.canRead()) {
                            sslContext = SSLContext.getInstance("TLS");
                            KeyStore ks = KeyStore.getInstance("PKCS12");
                            ks.load(new FileInputStream(new File("etc/site.p12")), secret);
                            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                            kmf.init(ks, secret);
                            sslContext.init(kmf.getKeyManagers(), null, null);
                        }
                    } catch (Exception e) {
                        System.out.println("Unable to create SSLContext");
                        e.printStackTrace();
                    }
                }
            }
        }
        return sslContext;
    }

    public static SSLEngine createSSLEngine() {
        SSLContext context = get();
        if (context == null)
            return null;

        return context.createSSLEngine();
    }

}
