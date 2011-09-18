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
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class DefaultPreset extends PreviewPreset {

    public DefaultPreset() {
        super(NbBundle.getMessage(DefaultPreset.class, "Default.name"));


        properties.put(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);

        properties.put(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.FALSE);
        properties.put(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
        properties.put(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);

        properties.put(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
        properties.put(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);

        properties.put(PreviewProperty.NODE_LABEL_FONT, Font.decode("Arial Plain 12"));
        properties.put(PreviewProperty.EDGE_LABEL_SHORTEN, Boolean.FALSE);
        properties.put(PreviewProperty.NODE_LABEL_MAX_CHAR, 10);
        properties.put(PreviewProperty.NODE_BORDER_WIDTH, 1.0f);
        properties.put(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(Color.BLACK));

    }
}
