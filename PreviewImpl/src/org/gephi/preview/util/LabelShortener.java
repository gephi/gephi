package org.gephi.preview.util;

/**
 *
 * @author jeremy
 */
public abstract class LabelShortener {

    public static void shortenLabel(LabelShortenerClient client, int maxChar) {
        String originalValue = client.getOriginalValue();
        String value = originalValue.length() > maxChar ?
            (originalValue.substring(0, maxChar - 1) + "…") : originalValue;

        client.setValue(value);
    }
}
