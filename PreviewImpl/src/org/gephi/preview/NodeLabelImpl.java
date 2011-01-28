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
package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.NodeLabel;
import org.gephi.preview.updaters.LabelFontAdjusterClient;
import org.gephi.preview.updaters.LabelShortenerClient;

/**
 * Implementation  of a preview node label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeLabelImpl extends AbstractNodeChild
        implements NodeLabel, LabelShortenerClient, LabelFontAdjusterClient {

    private final String originalValue;
    private final float labelSizeFactor;
    private String value;
    private Font font;

    /**
     * Constructor.
     *
     * @param parent  the label's parent node
     * @param value   the label value
     */
    public NodeLabelImpl(NodeImpl parent, String value, float labelSize) {
        super(parent);
        this.originalValue = value;
        this.labelSizeFactor = labelSize;
    }

    public Font getFont() {
        return font;
    }

    public Font getBaseFont() {
        return parent.getBaseLabelFont();
    }

    public float getSizeFactor() {
        return labelSizeFactor;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void revertOriginalValue() {
        setValue(originalValue);
    }
}
