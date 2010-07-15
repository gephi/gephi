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
package org.gephi.ui.filters.plugin.hierarchy;

import javax.swing.JPanel;
import org.gephi.filters.plugin.hierarchy.LevelBuilder.LevelFilter;
import org.gephi.filters.plugin.hierarchy.LevelUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = LevelUI.class)
public class LevelUIImpl implements LevelUI {

    public JPanel getPanel(LevelFilter filter) {
        LevelPanel levelPanel = new LevelPanel();
        levelPanel.setup(filter);
        return levelPanel;
    }
}
