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
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Skeletal implementation of {@link GlyphProducer}.
 */
abstract class AbstractGlyphProducer implements GlyphProducer {

    /**
     * Reusable array for creating glyph vectors for a single character.
     */
    /*@Nonnull*/
    private final char[] characters = new char[1];

    /**
     * Font glyphs made from.
     */
    /*@Nonnull*/
    private final Font font;

    /**
     * Rendering controller.
     */
    /*@Nonnull*/
    private final RenderDelegate renderDelegate;

    /**
     * Font render details.
     */
    /*@Nonnull*/
    private final FontRenderContext fontRenderContext;

    /**
     * Cached glyph vectors.
     */
    /*@Nonnull*/
    private final Map<String, GlyphVector> glyphVectors = new HashMap<String, GlyphVector>();

    /**
     * Returned glyphs.
     */
    /*@Nonnull*/
    private final List<Glyph> output = new ArrayList<Glyph>();

    /**
     * View of glyphs.
     */
    /*@Nonnull*/
    private final List<Glyph> outputView = Collections.unmodifiableList(output);

    /**
     * Constructs an abstract glyph producer.
     *
     * @param font Font glyphs will be made from
     * @param rd Object for controlling rendering
     * @param frc Details on how to render fonts
     * @throws NullPointerException if font, render delegate, or font render context is null
     */
    AbstractGlyphProducer(/*@Nonnull*/ final Font font,
                          /*@Nonnull*/ final RenderDelegate rd,
                          /*@Nonnull*/ final FontRenderContext frc) {

        Check.notNull(font, "Font cannot be null");
        Check.notNull(rd, "Render delegate cannot be null");
        Check.notNull(frc, "Font render context cannot be null");

        this.font = font;
        this.renderDelegate = rd;
        this.fontRenderContext = frc;
    }

    /**
     * Adds outer space around a rectangle.
     *
     * <p>
     * This method was formally called "normalize."
     *
     * <p>
     * Give ourselves a boundary around each entity on the backing store in order to prevent
     * bleeding of nearby Strings due to the fact that we use linear filtering
     *
     * <p>
     * Note that this boundary is quite heuristic and is related to how far away in 3D we may view
     * the text -- heuristically, 1.5% of the font's height.
     *
     * @param src Original rectangle
     * @param font Font being used to create glyphs
     * @return Rectangle with margin added, not null
     * @throws NullPointerException if rectangle or font is null
     */
    /*@Nonnull*/
    private static Rectangle2D addMarginTo(/*@Nonnull*/ final Rectangle2D src,
                                           /*@Nonnull*/ final Font font) {

        final int boundary = (int) Math.max(1, 0.015 * font.getSize());
        final int x = (int) Math.floor(src.getMinX() - boundary);
        final int y = (int) Math.floor(src.getMinY() - boundary);
        final int w = (int) Math.ceil(src.getWidth() + 2 * boundary);
        final int h = (int) Math.ceil(src.getHeight() + 2 * boundary);;

        return new Rectangle2D.Float(x, y, w, h);
    }

    /**
     * Adds inner space to a rectangle.
     *
     * <p>
     * This method was formally called "preNormalize."
     *
     * <p>
     * Need to round to integer coordinates.
     *
     * <p>
     * Also give ourselves a little slop around the reported bounds of glyphs because it looks like
     * neither the visual nor the pixel bounds works perfectly well.
     *
     * @param src Original rectangle
     * @return Rectangle with padding added, not null
     * @throws NullPointerException if rectangle is null
     */
    /*@Nonnull*/
    private static Rectangle2D addPaddingTo(/*@Nonnull*/ final Rectangle2D src) {

        final int minX = (int) Math.floor(src.getMinX()) - 1;
        final int minY = (int) Math.floor(src.getMinY()) - 1;
        final int maxX = (int) Math.ceil(src.getMaxX()) + 1;
        final int maxY = (int) Math.ceil(src.getMaxY()) + 1;

        return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Adds a glyph to the reusable list for output.
     *
     * @param glyph Glyph to add to output
     * @throws NullPointerException if glyph is null
     */
    protected final void addToOutput(/*@Nonnull*/ final Glyph glyph) {
        Check.notNull(glyph, "Glyph cannot be null");
        output.add(glyph);
    }

    /**
     * Clears the reusable list for output.
     */
    protected final void clearOutput() {
        output.clear();
    }

    /**
     * Makes a glyph vector for a character.
     *
     * @param c Character to create glyph vector from
     * @return Glyph vector for the character, not null
     */
    /*@Nonnull*/
    protected final GlyphVector createGlyphVector(final char c) {
        characters[0] = c;
        return font.createGlyphVector(fontRenderContext, characters);
    }

    /**
     * Makes a glyph vector for a string.
     *
     * @param font Style of text
     * @param frc Details on how to render font
     * @param str Text as a string
     * @return Glyph vector for the string, not null
     * @throws NullPointerException if string is null
     */
    /*@Nonnull*/
    protected final GlyphVector createGlyphVector(/*@Nonnull*/ final String str) {

        Check.notNull(str, "String cannot be null");

        GlyphVector gv = glyphVectors.get(str);

        // Check if already made
        if (gv != null) {
            return gv;
        }

        // Otherwise make and store it
        final char[] text = str.toCharArray();
        final int len = str.length();
        gv = font.layoutGlyphVector(fontRenderContext, text, 0, len, 0);
        glyphVectors.put(str, gv);
        return gv;
    }

    /*@CheckForSigned*/
    @Override
    public final float findAdvance(final char c) {

        // Check producer's inventory first
        final Glyph glyph = createGlyph(c);
        if (glyph != null) {
            return glyph.advance;
        }

        // Otherwise create the glyph vector
        final GlyphVector gv = createGlyphVector(c);
        final GlyphMetrics gm = gv.getGlyphMetrics(0);
        return gm.getAdvance();
    }

    /*@Nonnull*/
    @Override
    public final Rectangle2D findBounds(/*@Nonnull*/ final String str) {

        Check.notNull(str, "String cannot be null");

        final List<Glyph> glyphs = createGlyphs(str);

        // Check if already computed bounds
        if (glyphs.size() == 1) {
            final Glyph glyph = glyphs.get(0);
            return glyph.bounds;
        }

        // Otherwise just recompute it
        return addPaddingTo(renderDelegate.getBounds(str, font, fontRenderContext));
    }

    /**
     * Returns the font used to create glyphs.
     *
     * @return Font used to create glyphs, not null
     */
    /*@Nonnull*/
    protected final Font getFont() {
        return font;
    }

    /**
     * Returns a read-only view of this producer's reusable list for output.
     *
     * @return Read-only view of reusable list, not null
     */
    /*@Nonnull*/
    protected final List<Glyph> getOutput() {
        return outputView;
    }

    /**
     * Checks if any characters in a string require full layout.
     *
     * <p>
     * The process of creating and laying out glyph vectors is relatively complex and can slow down
     * text rendering significantly.  This method is intended to increase performance by not
     * creating glyph vectors for strings with characters that can be treated independently.
     *
     * <p>
     * Currently the decision is very simple.  It just treats any characters above the <i>IPA
     * Extensions</i> block as complex.  This is convenient because most Latin characters are
     * treated as simple but <i>Spacing Modifier Letters</i> and <i>Combining Diacritical Marks</i>
     * are not.  Ideally it would also be nice to have a few other blocks included, especially
     * <i>Greek</i> and maybe symbols, but that is perhaps best left for later work.
     *
     * <p>
     * A truly correct implementation may require a lot of research or developers with more
     * experience in the area.  However, the following Unicode blocks are known to require full
     * layout in some form:
     *
     * <ul>
     * <li>Spacing Modifier Letters (02B0-02FF)
     * <li>Combining Diacritical Marks (0300-036F)
     * <li>Hebrew (0590-05FF)
     * <li>Arabic (0600-06FF)
     * <li>Arabic Supplement (0750-077F)
     * <li>Combining Diacritical Marks Supplement (1DC0-1FFF)
     * <li>Combining Diacritical Marks for Symbols (20D0-20FF)
     * <li>Arabic Presentation Forms-A (FB50–FDFF)
     * <li>Combining Half Marks (FE20–FE2F)
     * <li>Arabic Presentation Forms-B (FE70–FEFF)
     * </ul>
     *
     * <p>
     * Asian scripts will also have letters that combine together, but it appears that the input
     * method may take care of that so it may not be necessary to check for them here.
     *
     * <p>
     * Finally, it should be noted that even Latin has characters that can combine into glyphs
     * called ligatures.  The classic example is an 'f' and an 'i'.  Java however will not make the
     * replacements itself so we do not need to consider that here.
     *
     * @param str Text of unknown character types
     * @return True if a complex character is found
     * @throws NullPointerException if string is null
     */
    protected static boolean hasComplexCharacters(/*@Nonnull*/ final String str) {

        Check.notNull(str, "String cannot be null");

        final int len = str.length();
        for (int i = 0; i < len; ++i) {
            if (str.charAt(i) > 0x2AE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a glyph vector is complex.
     *
     * @param gv Glyph vector to check
     * @return True if glyph vector is complex
     * @throws NullPointerException if glyph vector is null
     */
    protected static boolean isComplex(/*@CheckForNull*/ final GlyphVector gv) {

        Check.notNull(gv, "Glyph vector cannot be null");

        return gv.getLayoutFlags() != 0;
    }

    /**
     * Measures a glyph.
     *
     * <p>
     * Sets all the measurements in a glyph after it's created.
     *
     * @param glyph Visual representation of a character
     * @throws NullPointerException if glyph is null
     */
    protected final void measure(/*@Nonnull*/ final Glyph glyph) {

        Check.notNull(glyph, "Glyph cannot be null");

        // Compute visual boundary
        final Rectangle2D visualBox;
        if (glyph.str != null) {
            visualBox = renderDelegate.getBounds(glyph.str, font, fontRenderContext);
        } else {
            visualBox = renderDelegate.getBounds(glyph.glyphVector, fontRenderContext);
        }

        // Compute rectangles
        final Rectangle2D paddingBox = addPaddingTo(visualBox);
        final Rectangle2D marginBox = addMarginTo(paddingBox, font);

        // Set fields
        glyph.padding = new Glyph.Boundary(paddingBox, visualBox);
        glyph.margin = new Glyph.Boundary(marginBox, paddingBox);
        glyph.width = (float) paddingBox.getWidth();
        glyph.height = (float) paddingBox.getHeight();
        glyph.ascent = (float) paddingBox.getMinY() * -1;
        glyph.descent = (float) paddingBox.getMaxY();
        glyph.kerning = (float) paddingBox.getMinX();
        glyph.bounds = paddingBox;
    }
}
