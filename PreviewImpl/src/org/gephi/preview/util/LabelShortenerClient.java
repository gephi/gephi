package org.gephi.preview.util;

/**
 * Classes implementing this interface are able to have their label shortened.
 * 
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface LabelShortenerClient {

    /**
     * Returns the label's original value.
     *
     * @return the label's original value
     */
    public String getOriginalValue();

    /**
     * Defines the label's current value.
     * 
     * @param value  the label's current value to set
     */
    public void setValue(String value);
}
