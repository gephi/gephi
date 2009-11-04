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
package org.gephi.ui.partition.transformer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.Transformer;
import org.gephi.partition.api.TransformerBuilder;
import org.gephi.partition.api.TransformerUI;
import org.gephi.partition.transformer.EdgeColorTransformer;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeColorTransformerBuilder implements TransformerBuilder.Edge {

    public Transformer getTransformer() {
        return new EdgeColorTransformer();
    }

    public TransformerUI getUI() {
        return new UI();
    }

    private static class UI implements TransformerUI {

        private EdgeColorTransformerPanel panel;

        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/org/gephi/ui/partition/transformer/color.png"));
        }

        public String getName() {
            return NbBundle.getMessage(NodeColorTransformerBuilder.class, "EdgeColorTransformerBuilder.ui.name");
        }

        public JPanel getPanel() {
            panel = new EdgeColorTransformerPanel();
            return panel;
        }

        public void setup(Partition partition, Transformer transformer) {
        }

        public void unsetup() {
            panel = null;
        }
    }
}
