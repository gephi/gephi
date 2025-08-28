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

import com.jogamp.opengl.util.awt.TextRenderer.RenderDelegate;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.List;


/**
 * {@link GlyphProducer} that creates glyphs in the ASCII range.
 */
/*@NotThreadSafe*/
final class AsciiGlyphProducer extends AbstractGlyphProducer {

    /**
     * Storage for glyphs.
     */
    /*@Nonnull*/
    private final Glyph[] inventory = new Glyph[128];

    /**
     * Constructs an {@link AsciiGlyphProducer}.
     *
     * @param font Font glyphs will be made from
     * @param rd Delegate for controlling rendering
     * @param frc Details on how to render fonts
     * @throws NullPointerException if font, render delegate, or font render context is null
     */
    AsciiGlyphProducer(/*@Nonnull*/ final Font font,
                       /*@Nonnull*/ final RenderDelegate rd,
                       /*@Nonnull*/ final FontRenderContext frc) {
        super(font, rd, frc);
    }

    @Override
    public void clearGlyphs() {
        // empty
    }

    /*@Nonnull*/
    @Override
    public Glyph createGlyph(char c) {

        // Check if out of range
        if (c > 128) {
            c = '_';
        }

        // Check if already created
        Glyph glyph = inventory[c];
        if (glyph != null) {
            return glyph;
        }

        // Create glyph
        GlyphVector gv = createGlyphVector(c);
        glyph = new Glyph(c, gv);
        measure(glyph);

        // Store and finish
        inventory[c] = glyph;
        return glyph;
    }

    /*@Nonnull*/
    @Override
    public List<Glyph> createGlyphs(/*@Nonnull*/ final String str) {

        Check.notNull(str, "String cannot be null");

        // Clear the output
        clearOutput();

        // Add each glyph to the output
        final int len = str.length();
        for (int i = 0; i < len; ++i) {
            final char character = str.charAt(i);
            final Glyph glyph = createGlyph(character);
            addToOutput(glyph);
        }

        // Return the output
        return getOutput();
    }

    @Override
    public void removeGlyph(/*@CheckForNull*/ final Glyph glyph) {
        // empty
    }
}
