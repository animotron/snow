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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MessageDiggester {

    private static char[] hex =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final String CACHE_ALGORITHM = "SHA-1";

    public static MessageDigest md() {
        try {
            return MessageDigest.getInstance(CACHE_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            //can't be, but throw runtime error
            throw new RuntimeException(e);
        }
    }

    public static String byteArrayToHex( byte[] b ) {
        char[] buff = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            buff[i * 2] = hex[v >>> 4];
            buff[i * 2 + 1] = hex[v & 0x0f];
        }
        return new String(buff);
    }

    public static byte[] longToByteArray( long b ) {
        byte[] target = new byte[8];
        for ( int i = 0; i < 8; i++ ) {
            target[7-i] = (byte) (b >>> (i * 8));
        }
        return target;
    }

    public static String longToHex( long b ) {
        return byteArrayToHex(longToByteArray(b));
    }

    public static final MessageDigest clone(MessageDigest md) {
        try {
            return (MessageDigest) md.clone();
        } catch (CloneNotSupportedException e) {
            //can't be, but throw runtime error
            throw new RuntimeException(e);
        }
    }

}
