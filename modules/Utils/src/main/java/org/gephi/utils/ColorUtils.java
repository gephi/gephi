package org.gephi.utils;

import java.awt.Color;
import java.nio.ByteBuffer;

public class ColorUtils {

    public static Color darken(Color color, double factor) {
        if (factor >= 1.0) {
            throw new IllegalArgumentException("Factor must be < 1.0 to darken");
        }
        return darkenOrLighten(color, factor);
    }

    public static Color lighten(Color color, double factor) {
        if (factor <= 1.0) {
            throw new IllegalArgumentException("Factor must be > 1.0 to lighten");
        }
        return darkenOrLighten(color, factor);
    }

    private static Color darkenOrLighten(Color color, double factor) {
        // factor < 1.0 makes it darker, > 1.0 would make it lighter
        int r = (int) Math.round(color.getRed()   * factor);
        int g = (int) Math.round(color.getGreen() * factor);
        int b = (int) Math.round(color.getBlue()  * factor);

        // Clamp to valid range [0, 255]
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        // Preserve alpha
        return new Color(r, g, b, color.getAlpha());
    }

    /**
     * Serializes a Color to a 4-byte array (ARGB packed int).
     */
    public static byte[] serializeColor(Color color) {
        return ByteBuffer.allocate(4).putInt(color.getRGB()).array();
    }

    /**
     * Deserializes a Color from a 4-byte array (ARGB packed int).
     *
     * @return the deserialized Color, or null if bytes is null or invalid
     */
    public static Color deserializeColor(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return null;
        }
        return new Color(ByteBuffer.wrap(bytes).getInt(), true);
    }

    /**
     * Serializes a Color array to bytes (4 bytes per color, ARGB packed int).
     */
    public static byte[] serializeColors(Color[] colors) {
        ByteBuffer buffer = ByteBuffer.allocate(colors.length * 4);
        for (Color c : colors) {
            buffer.putInt(c.getRGB());
        }
        return buffer.array();
    }

    /**
     * Deserializes a Color array from bytes (4 bytes per color, ARGB packed int).
     *
     * @return the deserialized Color array, or null if bytes is null or invalid
     */
    public static Color[] deserializeColors(byte[] bytes) {
        if (bytes == null || bytes.length % 4 != 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Color[] colors = new Color[bytes.length / 4];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color(buffer.getInt(), true);
        }
        return colors;
    }

    /**
     * Serializes a float array to bytes (4 bytes per float). Useful for gradient color positions.
     */
    public static byte[] serializeFloats(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 4);
        for (float v : values) {
            buffer.putFloat(v);
        }
        return buffer.array();
    }

    /**
     * Deserializes a float array from bytes (4 bytes per float). Useful for gradient color positions.
     *
     * @return the deserialized float array, or null if bytes is null or invalid
     */
    public static float[] deserializeFloats(byte[] bytes) {
        if (bytes == null || bytes.length % 4 != 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] values = new float[bytes.length / 4];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }
}
