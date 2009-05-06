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

package org.gephi.data.laboratory;

import org.gephi.data.laboratory.NodeTreeModel.TreeNode;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeRenderer implements RenderDataProvider {



    @Override
    public java.awt.Color getBackground(Object o) {
        return null;
    }

    @Override
    public String getDisplayName(Object o) {
        return ((TreeNode)o).getLabel();
    }

    @Override
    public java.awt.Color getForeground(Object o) {
        return null;
    }

    @Override
    public javax.swing.Icon getIcon(Object o) {
        return null;

    }

    @Override
    public String getTooltipText(Object o) {
        return "";
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }
}
