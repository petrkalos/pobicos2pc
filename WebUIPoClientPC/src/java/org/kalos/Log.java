/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kalos;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author kalos
 */
public class Log {

    public static final LinkedBlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();

    public static void w(String TAG, String string) {

        msgQueue.add(string);
    }

    public static void e(String TAG, String string) {

        msgQueue.add(string);
    }

    public static void d(String TAG, String string) {

        msgQueue.add(string);
    }
}
