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
import java.lang.Character.UnicodeBlock;


/**
 * Utility for working with {@link GlyphProducer}'s.
 */
/*@ThreadSafe*/
public final class GlyphProducers {

    /**
     * Prevents instantiation.
     */
    private GlyphProducers() {
        // empty
    }

    /**
     * Creates a {@link GlyphProducer} based on a range of characters.
     *
     * @param font Style of text
     * @param rd Controller of rendering details
     * @param frc Details on how fonts are rendered
     * @param ub Range of characters to support
     * @return Correct glyph producer for unicode block, not null
     * @throws NullPointerException if font, render delegate, or render context is null
     */
    /*@Nonnull*/
    public static GlyphProducer get(/*@Nonnull*/ final Font font,
                                    /*@Nonnull*/ final RenderDelegate rd,
                                    /*@Nonnull*/ final FontRenderContext frc,
                                    /*@CheckForNull*/ final UnicodeBlock ub) {

        Check.notNull(font, "Font cannot be null");
        Check.notNull(rd, "Render delegate cannot be null");
        Check.notNull(frc, "Font render context cannot be null");

        if (ub == UnicodeBlock.BASIC_LATIN) {
            return new AsciiGlyphProducer(font, rd, frc);
        } else {
            return new UnicodeGlyphProducer(font, rd, frc);
        }
    }
}
