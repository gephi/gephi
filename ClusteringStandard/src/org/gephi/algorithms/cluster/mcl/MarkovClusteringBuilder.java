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
package org.gephi.algorithms.cluster.mcl;

import javax.swing.JPanel;
import org.gephi.algorithms.cluster.api.Clusterer;
import org.gephi.algorithms.cluster.api.ClustererBuilder;
import org.gephi.algorithms.cluster.api.ClustererUI;

/**
 *
 * @author Mathieu Bastian
 */
public class MarkovClusteringBuilder implements ClustererBuilder {

    public Clusterer getClusterer() {
        return new MarkovClustering();
    }

    public String getName() {
        return "MCL";
    }

    public Class getClustererClass() {
        return MarkovClustering.class;
    }

    public ClustererUI getUI() {
        return new MarkovClusteringUI();
    }

    private static class MarkovClusteringUI implements ClustererUI {

        MarkovClusteringPanel panel;

        public JPanel getPanel() {
            panel = new MarkovClusteringPanel();
            return panel;
        }

        public void setup(Clusterer clusterer) {
            panel.setup(clusterer);
        }

        public void unsetup() {
            panel.unsetup();
            panel = null;
        }
    }
}
