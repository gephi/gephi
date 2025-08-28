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

import java.awt.geom.Rectangle2D;
import java.util.List;


/**
 * Utility for creating glyphs.
 */
public interface GlyphProducer {

    /**
     * Deletes all stored glyphs.
     */
    void clearGlyphs();

    /**
     * Makes a glyph for a single character.
     *
     * @param c Character
     * @return Reused instance of a glyph
     */
    Glyph createGlyph(char c);

    /**
     * Makes a glyph for each character in a string.
     *
     * @param str Text as a string
     * @return View of glyphs valid until next call
     * @throws NullPointerException if string is null
     */
    /*@Nonnull*/
    List<Glyph> createGlyphs(/*@Nonnull*/ String str);

    /**
     * Determines the distance to the next character after a glyph.
     *
     * @param c Character to find advance of
     * @return Distance to the next character after a glyph, which may be negative
     */
    /*@CheckForSigned*/
    float findAdvance(char c);

    /**
     * Determines the visual bounds of a string with padding added.
     *
     * @param str Text to find visual bounds of
     * @return Visual bounds of string with padding added, not null
     * @throws NullPointerException if string is null
     */
    /*@Nonnull*/
    Rectangle2D findBounds(/*@Nonnull*/ String str);

    /**
     * Deletes a single stored glyph.
     *
     * @param glyph Previously created glyph, ignored if null
     */
    void removeGlyph(/*@CheckForNull*/ Glyph glyph);
}
