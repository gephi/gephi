package org.gephi.viz.engine.util.gl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Eduardo Ramos
 */
public final class Buffers {

    public static int bufferElementBytes(Buffer buf) {
        if (buf instanceof FloatBuffer) {
            return Float.BYTES;
        }
        if (buf instanceof IntBuffer) {
            return Integer.BYTES;
        }
        if (buf instanceof ShortBuffer) {
            return Short.BYTES;
        }
        if (buf instanceof ByteBuffer) {
            return Byte.BYTES;
        }
        if (buf instanceof DoubleBuffer) {
            return Double.BYTES;
        }
        if (buf instanceof LongBuffer) {
            return Long.BYTES;
        }
        if (buf instanceof CharBuffer) {
            return Character.BYTES;
        }

        return 1;
    }
}
