package org.gephi.preview.api.util;

/**
 * Interface providing methods to render an horizontal alignment for different
 * supports.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface HAlign {

    /**
     * Formats the alignment for a Processing target.
     *
     * @return the formatted alignment
     */
    public int toProcessing();

    /**
     * Formats the alignment for a CSS target.
     *
     * @return the formatted alignment
     */
    public String toCSS();

    /**
     * Formats the alignment for an iText target.
     *
     * @return the formatted alignment
     */
    public int toIText();
}
