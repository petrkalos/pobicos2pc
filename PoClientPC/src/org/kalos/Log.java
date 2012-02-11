/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kalos;

/**
 *
 * @author kalos
 */
public class Log {

    public static void w(String TAG, String string) {
        System.out.println(TAG+" : "+string);
    }

    public static void e(String TAG, String string) {
        System.err.println(TAG+" : "+string);
    }

    public static void d(String TAG,String string) {
        System.out.println(TAG+" : "+string);
    }
    
}
