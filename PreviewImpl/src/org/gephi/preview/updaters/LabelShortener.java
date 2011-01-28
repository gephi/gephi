/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
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

    /**
     * Reverts the original value of the given label.
     *
     * @param client  the label to revert the original value
     */
    public static void revertLabel(LabelShortenerClient client) {
        client.revertOriginalValue();
    }
}
