package org.gephi.preview.updaters;

import java.awt.Font;

/**
 * Class to adjust label fonts.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class LabelFontAdjuster {

    /**
     * Adjusts the font of the given label client.
     *
     * @param client   the label to adjust the font
     */
    public static void adjustFont(LabelFontAdjusterClient client) {
        Font baseFont = client.getBaseFont();
        int newSize = (int) (baseFont.getSize() * client.getSizeFactor());
        Font font = new Font(baseFont.getName(), baseFont.getStyle(), newSize);

        client.setFont(font);
    }
}
