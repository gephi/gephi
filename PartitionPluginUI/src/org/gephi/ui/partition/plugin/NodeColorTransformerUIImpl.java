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
package org.gephi.ui.partition.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.partition.api.Partition;
import org.gephi.partition.plugin.spi.NodeColorTransformerUI;
import org.gephi.partition.spi.Transformer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = NodeColorTransformerUI.class)
public class NodeColorTransformerUIImpl implements NodeColorTransformerUI {

    private NodeColorTransformerPanel panel;

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/ui/partition/plugin/resources/color.png"));
    }

    public String getName() {
        return NbBundle.getMessage(NodeColorTransformerUIImpl.class, "NodeColorTransformerBuilder.ui.name");
    }

    public JPanel getPanel() {
        panel = new NodeColorTransformerPanel();
        return panel;
    }

    public void setup(Partition partition, Transformer transformer) {
        panel.setup(partition, transformer, true);
    }

    public void unsetup() {
        panel = null;
    }
}
