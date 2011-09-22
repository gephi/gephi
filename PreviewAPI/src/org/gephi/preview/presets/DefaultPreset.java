/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.presets;

import java.awt.Color;
import java.awt.Font;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class DefaultPreset extends PreviewPreset {

    public DefaultPreset() {
        super(NbBundle.getMessage(DefaultPreset.class, "Default.name"));

        properties.put(PreviewProperty.ARROW_SIZE, 3f);
        properties.put(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);

        properties.put(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.MIXED));
        properties.put(PreviewProperty.EDGE_CURVED, true);
        properties.put(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.FALSE);
        properties.put(PreviewProperty.EDGE_OPACITY, 100f);
        properties.put(PreviewProperty.EDGE_RADIUS, 0f);
        properties.put(PreviewProperty.EDGE_THICKNESS, 1f);

        properties.put(PreviewProperty.EDGE_LABEL_COLOR, new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
        properties.put(PreviewProperty.EDGE_LABEL_FONT, new Font("Arial", Font.PLAIN, 10));
        properties.put(PreviewProperty.EDGE_LABEL_MAX_CHAR, 30);
        properties.put(PreviewProperty.EDGE_LABEL_OUTLINE_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.EDGE_LABEL_OUTLINE_OPACITY, 80f);
        properties.put(PreviewProperty.EDGE_LABEL_OUTLINE_SIZE, 0);
        properties.put(PreviewProperty.EDGE_LABEL_SHORTEN, false);

        properties.put(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(Color.BLACK));
        properties.put(PreviewProperty.NODE_BORDER_WIDTH, 1.0f);
        properties.put(PreviewProperty.NODE_OPACITY, 100f);

        properties.put(PreviewProperty.NODE_LABEL_BOX_COLOR, new DependantColor(DependantColor.Mode.PARENT));
        properties.put(PreviewProperty.NODE_LABEL_BOX_OPACITY, 100f);
        properties.put(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
        properties.put(PreviewProperty.NODE_LABEL_FONT, new Font("Arial", Font.PLAIN, 12));
        properties.put(PreviewProperty.NODE_LABEL_MAX_CHAR, 30);
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_OPACITY, 80f);
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_SIZE, 0);
        properties.put(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, true);
        properties.put(PreviewProperty.NODE_LABEL_SHORTEN, false);
        properties.put(PreviewProperty.NODE_LABEL_SHOW_BOX, false);

        properties.put(PreviewProperty.SHOW_EDGES, Boolean.TRUE);
        properties.put(PreviewProperty.SHOW_EDGE_LABELS, Boolean.FALSE);
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
    }
}
