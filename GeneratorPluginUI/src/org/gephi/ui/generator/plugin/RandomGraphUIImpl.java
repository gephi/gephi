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
package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.generator.plugin.RandomGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RandomGraphUI.class)
public class RandomGraphUIImpl implements RandomGraphUI {

    private RandomGraphPanel panel;
    private RandomGraph randomGraph;

    public RandomGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new RandomGraphPanel();
        }
        return RandomGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.randomGraph = (RandomGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new RandomGraphPanel();
        }
        panel.nodeField.setText(String.valueOf(randomGraph.getNumberOfNodes()));
        panel.edgeField.setText(String.valueOf(randomGraph.getWiringProbability()));
    }

    public void unsetup() {
        //Set params
        randomGraph.setNumberOfNodes(Integer.parseInt(panel.nodeField.getText()));
        randomGraph.setWiringProbability(Double.parseDouble(panel.edgeField.getText()));
        panel = null;
    }
}
