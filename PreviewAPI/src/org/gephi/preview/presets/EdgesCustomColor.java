/*
Copyright 2008-2010 Gephi
Authors : Sebastien Heymann <sebastien.heymann@gephi.org>
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
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;

public class EdgesCustomColor extends PreviewPreset {

    public EdgesCustomColor() {
        super(NbBundle.getMessage(EdgesCustomColor.class, "EdgesCustomColor.name"));

        //Default
        DefaultPreset defaultPreset = new DefaultPreset();
        properties.putAll(defaultPreset.getProperties());
        
        //Custom values
        properties.put(PreviewProperty.NODE_LABEL_SHOW_BOX, Boolean.TRUE);
        properties.put(PreviewProperty.NODE_LABEL_BOX_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.NODE_LABEL_BOX_OPACITY, 80f);
        properties.put(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        properties.put(PreviewProperty.NODE_OPACITY, 0);
        properties.put(PreviewProperty.EDGE_COLOR, new EdgeColor(new Color(76, 168, 36)));
    }
}
