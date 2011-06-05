/*
Copyright 2008-2011 Gephi
Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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
package org.gephi.layout.plugin.forceAtlas2;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Layout Builder
 * @author Mathieu Jacomy
 */
@ServiceProvider(service = LayoutBuilder.class)
public class ForceAtlas2Builder implements LayoutBuilder {

    private ForceAtlas2UI ui = new ForceAtlas2UI();

    @Override
    public String getName() {
        return NbBundle.getMessage(ForceAtlas2.class, "ForceAtlas2.name");
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    @Override
    public ForceAtlas2 buildLayout() {
        ForceAtlas2 layout = new ForceAtlas2(this);
        return layout;
    }

    private class ForceAtlas2UI implements LayoutUI {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ForceAtlas2.class, "ForceAtlas2.description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 4;
        }

        @Override
        public int getSpeedRank() {
            return 4;
        }
    }
}