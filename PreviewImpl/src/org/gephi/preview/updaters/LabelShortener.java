package org.gephi.preview.updaters;

/**
 * Class to shorten label values.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class LabelShortener {

    /**
     * Shortens the value of the given label client.
     *
     * @param client   the label to shorten
     * @param maxChar  the length of the new label value
     */
    public static void shortenLabel(LabelShortenerClient client, int maxChar) {
        String originalValue = client.getOriginalValue();
        String value = originalValue.length() > maxChar ?
            (originalValue.substring(0, maxChar - 1) + "…") : originalValue;

        client.setValue(value);
    }
}
