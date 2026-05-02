package org.gephi.viz.engine.util;

/**
 *
 * @author Eduardo Ramos
 */
public class TimeUtils {

    public static long getTimeMillis() {
        return System.nanoTime() / 1_000_000;
    }
}
