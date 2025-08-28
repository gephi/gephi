/*
 * Copyright 2012 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package jogamp.opengl.util.awt.text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Utility for mapping text to glyphs.
 */
/*@NotThreadSafe*/
final class GlyphMap {

    /**
     * Fast map for ASCII chars.
     */
    /*@Nonnull*/
    private final Glyph[] ascii = new Glyph[128];

    /**
     * Map from char to code.
     */
    /*@Nonnull*/
    private final Map<Character, Integer> codes = new HashMap<Character, Integer>();

    /**
     * Map from code to glyph.
     */
    /*@Nonnull*/
    private final Map<Integer, Glyph> unicode = new HashMap<Integer, Glyph>();

    /**
     * Glyphs with layout flags.
     */
    /*@Nonnull*/
    private final Map<String, Glyph> complex = new HashMap<String, Glyph>();

    /**
     * Constructs a glyph map.
     */
    GlyphMap() {
        // empty
    }

    /**
     * Deletes all glyphs stored in the map.
     */
    void clear() {
        Arrays.fill(ascii, null);
        codes.clear();
        unicode.clear();
        complex.clear();
    }

    /**
     * Returns a glyph for a character.
     *
     * @param c Character to get glyph for
     * @return Glyph for the character, or null if it wasn't found
     */
    /*@CheckForNull*/
    Glyph get(final char c) {
        return (c < 128) ? ascii[c] : unicode.get(codes.get(c));
    }

    /**
     * Returns a glyph for a string.
     *
     * @param str String to get glyph for, which may be null
     * @return Glyph for the string, or null if it wasn't found
     */
    /*@CheckForNull*/
    Glyph get(/*@CheckForNull*/ final String str) {
        return complex.get(str);
    }

    /**
     * Stores a simple glyph in the map.
     *
     * @param c Character glyph represents
     * @param glyph Glyph to store
     * @throws NullPointerException if glyph is null
     */
    void put(final char c, /*@Nonnull*/ final Glyph glyph) {

        Check.notNull(glyph, "Glyph cannot be null");

        if (c < 128) {
            ascii[c] = glyph;
        } else {
            codes.put(c, glyph.code);
            unicode.put(glyph.code, glyph);
        }
    }

    /**
     * Stores a complex glyph in the map.
     *
     * @param str String glyph represents
     * @param glyph Glyph to store
     * @throws NullPointerException if string or glyph is null
     */
    void put(/*@Nonnull*/ final String str, /*@Nonnull*/ final Glyph glyph) {

        Check.notNull(str, "String cannot be null");
        Check.notNull(glyph, "Glyph cannot be null");

        complex.put(str, glyph);
    }

    /**
     * Deletes a simple glyph from this {@link GlyphMap}.
     *
     * @param c Character of glyph to remove
     */
    private void remove(final char c) {
        if (c < 128) {
            ascii[c] = null;
        } else {
            final Character character = c;
            final Integer code = codes.get(character);
            unicode.remove(code);
            codes.remove(character);
        }
    }

    /**
     * Deletes a single glyph from this {@link GlyphMap}.
     *
     * @param glyph Glyph to remove, ignored if null
     */
    void remove(/*@CheckForNull*/ final Glyph glyph) {

        if (glyph == null) {
            return;
        }

        if (glyph.str != null) {
            remove(glyph.str);
        } else {
            remove(glyph.character);
        }
    }

    /**
     * Deletes a complex glyph from this {@link GlyphMap}.
     *
     * @param str Text of glyph to remove, ignored if null
     */
    private void remove(/*@CheckForNull*/ final String str) {
        complex.remove(str);
    }
}
