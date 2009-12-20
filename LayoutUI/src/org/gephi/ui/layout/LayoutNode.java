/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.layout;

import java.util.HashMap;
import java.util.Map;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutNode extends AbstractNode {

    private Layout layout;
    private PropertySet[] propertySets;

    public LayoutNode(Layout layout) {
        super(Children.LEAF);
        this.layout = layout;
        setName(layout.getBuilder().getName());
    }

    @Override
    public PropertySet[] getPropertySets() {
        if (propertySets == null) {
            try {
                Map<String, Sheet.Set> sheetMap = new HashMap<String, Sheet.Set>();
                for (LayoutProperty layoutProperty : layout.getProperties()) {
                    Sheet.Set set = sheetMap.get(layoutProperty.getCategory());
                    if (set == null) {
                        set = Sheet.createPropertiesSet();
                        set.setDisplayName(layoutProperty.getCategory());
                        sheetMap.put(layoutProperty.getCategory(), set);
                    }
                    set.put(layoutProperty.getProperty());
                }
                propertySets = sheetMap.values().toArray(new PropertySet[0]);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return propertySets;
    }

    public Layout getLayout() {
        return layout;
    }
}
