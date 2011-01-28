/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.layout.spi;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Various information about a layout algorithm that allows UI integration.
 *
 * @author Mathieu Bastian
 */
public interface LayoutUI {

    /**
     * The description of the layout algorithm purpose.
     * @return  a description snippet for the algorithm
     */
    public String getDescription();

    /**
     * The icon that represents the layout action.
     * @return  a icon for this particular layout
     */
    public Icon getIcon();

    /**
     * A <code>LayoutUI</code> can have a optional settings panel, that will be
     * displayed instead of the property sheet.
     * @param layout the layout that require a simple panel
     * @return A simple settings panel for <code>layout</code> or
     * <code>null</code>
     */
    public JPanel getSimplePanel(Layout layout);

    /**
     * An appraisal of quality for this algorithm. The rank must be between 1 and
     * 5. The rank will be displayed tousers to help them to choose a suitable
     * algorithm. Return -1 if you don't want to display a rank.
     * @return an integer between 1 and 5 or -1 if you don't want to show a rank
     */
    public int getQualityRank();

    /**
     * An appraisal of speed for this algorithm. The rank must be between 1 and
     * 5. The rank will be displayed tousers to help them to choose a suitable
     * algorithm. Return -1 if you don't want to display a rank.
     * @return an integer between 1 and 5 or -1 if you don't want to show a rank
     */
    public int getSpeedRank();
}
