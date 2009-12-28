package org.gephi.preview.api.util;

/**
 * Interface providing methods to render an horizontal alignment for different
 * supports.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface HAlign {

    /**
     * Formats the alignment as a string for a Processing target.
     *
     * @return the alignment formatted as a string
     */
    public int toProcessing();

    /**
     * Formats the alignment as a string for a CSS target.
     *
     * @return the alignment formatted as a string
     */
    public String toCSS();
}
