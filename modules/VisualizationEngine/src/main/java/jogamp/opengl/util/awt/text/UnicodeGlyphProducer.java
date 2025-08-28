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
 * {@link GlyphProducer} for creating glyphs of all characters in the basic unicode block.
 */
/*@NotThreadSafe*/
final class UnicodeGlyphProducer extends AbstractGlyphProducer {

    /**
     * Storage for glyphs.
     */
    /*@Nonnull*/
    private final GlyphMap glyphMap = new GlyphMap();

    /**
     * Constructs a {@link UnicodeGlyphProducer}.
     *
     * @param font Font glyphs will be made of
     * @param rd Object for controlling rendering
     * @param frc Details on how to render fonts
     * @throws NullPointerException if font, render delegate, or font render context is null
     */
    UnicodeGlyphProducer(/*@Nonnull*/ final Font font,
                         /*@Nonnull*/ final RenderDelegate rd,
                         /*@Nonnull*/ final FontRenderContext frc) {
        super(font, rd, frc);
    }

    @Override
    public void clearGlyphs() {
        glyphMap.clear();
    }

    /**
     * Creates a single glyph from text with a complex layout.
     *
     * @param str Text with a complex layout
     * @param gv Glyph vector of entire text
     * @return Read-only pointer to list of glyphs valid until next call
     * @throws NullPointerException if string is null
     * @throws NullPointerException if glyph vector is null
     */
    /*@Nonnull*/
    private List<Glyph> createComplexGlyph(/*@Nonnull*/ final String str,
                                           /*@Nonnull*/ final GlyphVector gv) {

        Check.notNull(str, "String cannot be null");
        Check.notNull(gv, "Glyph vector be null");

        clearOutput();

        // Create the glyph and add it to output
        Glyph glyph = glyphMap.get(str);
        if (glyph == null) {
            glyph = new Glyph(str, gv);
            measure(glyph);
            glyphMap.put(str, glyph);
        }
        addToOutput(glyph);

        return getOutput();
    }

    /*@Nonnull*/
    @Override
    public Glyph createGlyph(final char c) {
        Glyph glyph = glyphMap.get(c);
        if (glyph == null) {
            glyph = createGlyphImpl(c);
        }
        return glyph;
    }

    /*@Nonnull*/
    private Glyph createGlyphImpl(final char c) {

        // Create a glyph from the glyph vector
        final GlyphVector gv = createGlyphVector(c);
        final Glyph glyph = new Glyph(c, gv);

        // Measure and store it
        measure(glyph);
        glyphMap.put(c, glyph);

        return glyph;
    }

    /*@Nonnull*/
    @Override
    public List<Glyph> createGlyphs(/*@Nonnull*/ final String str) {

        Check.notNull(str, "String cannot be null");

        if (!hasComplexCharacters(str)) {
            return createSimpleGlyphs(str);
        } else {
            final GlyphVector gv = createGlyphVector(str);
            return isComplex(gv) ?  createComplexGlyph(str, gv) : createSimpleGlyphs(str);
        }
    }

    /**
     * Creates multiple glyphs from text with a simple layout.
     *
     * @param str Text with a simple layout
     * @return Read-only pointer to list of glyphs valid until next call
     * @throws NullPointerException if string is null
     */
    /*@Nonnull*/
    private List<Glyph> createSimpleGlyphs(/*@Nonnull*/ final String str) {

        Check.notNull(str, "String cannot be null");

        clearOutput();

        // Create the glyphs and add them to the output
        final int len = str.length();
        for (int i = 0; i < len; ++i) {
            final char c = str.charAt(i);
            final Glyph glyph = createGlyph(c);
            addToOutput(glyph);
        }

        return getOutput();
    }

    @Override
    public void removeGlyph(/*@CheckForNull*/ final Glyph glyph) {
        glyphMap.remove(glyph);
    }
}
