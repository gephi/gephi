/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.rotate;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
@ServiceProvider(service=LayoutBuilder.class)
public class ClockwiseRotate implements LayoutBuilder {

    private ClockwiseRotateLayoutUI ui = new ClockwiseRotateLayoutUI();

    public Layout buildLayout() {
        return new RotateLayout(this, -90);
    }

    public String getName() {
        return NbBundle.getMessage(ClockwiseRotate.class, "clockwise.name");
    }

    public LayoutUI getUI() {
        return ui;
    }

    private static class ClockwiseRotateLayoutUI implements LayoutUI {

        public String getDescription() {
            return NbBundle.getMessage(ClockwiseRotate.class, "clockwise.description");
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return -1;
        }

        public int getSpeedRank() {
            return -1;
        }
    }
}
