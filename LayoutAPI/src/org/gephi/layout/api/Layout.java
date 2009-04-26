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

package org.gephi.layout.api;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.network.api.LayoutReader;
import org.gephi.graph.api.EdgeLayoutInterface;
import org.gephi.graph.api.NodeLayoutInterface;

/**
 *
 * @author Mathieu Bastian
 */
public interface Layout<T extends NodeLayoutInterface ,U extends EdgeLayoutInterface> {

    public String getName();

    public String getDescription();

    public Icon getIcon();

    public boolean testAlgo();

    public void initAlgo();

    public void goAlgo(LayoutReader<T,U> reader);

    public void endAlgo();

    public LayoutProperty[] getProperties();

    public void resetPropertiesValues();

    public JPanel getPanel();
}
