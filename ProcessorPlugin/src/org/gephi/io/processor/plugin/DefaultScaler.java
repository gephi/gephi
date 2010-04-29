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
package org.gephi.io.processor.plugin;

import java.util.Collection;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.processor.spi.Scaler;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Scaler.class)
public class DefaultScaler implements Scaler {

    private float sizeMinimum;
    private float sizeMaximum;
    private float weightMinimum;
    private float weightMaximum;
    private float octreeLimit;

    private void setDefaults() {
        sizeMaximum = 100f;
        sizeMinimum = 4f;
        weightMinimum = 0.4f;
        weightMaximum = 2f;
        octreeLimit = 5000;
    }

    public void doScale(Container container) {
        setDefaults();

        float sizeMin = Float.POSITIVE_INFINITY;
        float sizeMax = Float.NEGATIVE_INFINITY;
        float xMin = Float.POSITIVE_INFINITY;
        float xMax = Float.NEGATIVE_INFINITY;
        float yMin = Float.POSITIVE_INFINITY;
        float yMax = Float.NEGATIVE_INFINITY;
        float zMin = Float.POSITIVE_INFINITY;
        float zMax = Float.NEGATIVE_INFINITY;
        float sizeRatio = 0f;
        float averageSize = 2.5f;

        Collection<? extends NodeDraftGetter> nodes = container.getUnloader().getNodes();

        //Recenter
        double centroidX = 0;
        double centroidY = 0;
        for (NodeDraftGetter node : nodes) {
            centroidX += node.getX();
            centroidY += node.getY();
        }
        centroidX /= nodes.size();
        centroidY /= nodes.size();
        for (NodeDraftGetter node : nodes) {
            node.setX((float) (node.getX() - centroidX));
            node.setY((float) (node.getY() - centroidY));
        }

        //Measure
        for (NodeDraftGetter node : nodes) {
            sizeMin = Math.min(node.getSize(), sizeMin);
            sizeMax = Math.max(node.getSize(), sizeMax);
            xMin = Math.min(node.getX(), xMin);
            xMax = Math.max(node.getX(), xMax);
            yMin = Math.min(node.getY(), yMin);
            yMax = Math.max(node.getY(), yMax);
            zMin = Math.min(node.getZ(), zMin);
            zMax = Math.max(node.getZ(), zMax);
        }

        if (sizeMin != 0 && sizeMax != 0) {

            if (sizeMin == sizeMax) {
                sizeRatio = sizeMinimum / sizeMin;
            } else {
                sizeRatio = (sizeMaximum - sizeMinimum) / (sizeMax - sizeMin);
            }

            //Watch octree limit
            if (xMin * sizeRatio < -octreeLimit) {
                sizeRatio = octreeLimit / xMin;
            }
            if (xMax * sizeRatio > octreeLimit) {
                sizeRatio = octreeLimit / xMax;
            }
            if (yMin * sizeRatio < -octreeLimit) {
                sizeRatio = octreeLimit / yMin;
            }
            if (yMax * sizeRatio > octreeLimit) {
                sizeRatio = octreeLimit / yMax;
            }
            if (zMin * sizeRatio < -octreeLimit) {
                sizeRatio = octreeLimit / zMin;
            }
            if (zMax * sizeRatio > octreeLimit) {
                sizeRatio = octreeLimit / zMax;
            }

            averageSize = 0f;

            //Scale node size
            for (NodeDraftGetter node : nodes) {
                float size = (node.getSize() - sizeMin) * sizeRatio + sizeMinimum;
                node.setSize(size);
                node.setX(node.getX() * sizeRatio);
                node.setY(node.getY() * sizeRatio);
                node.setZ(node.getZ() * sizeRatio);
                averageSize += size;
            }
            averageSize /= container.getUnloader().getNodes().size();
        }
        /*
        float weightMin = Float.POSITIVE_INFINITY;
        float weightMax = Float.NEGATIVE_INFINITY;
        float weightRatio = 0f;

        //Measure
        weightMaximum = averageSize * 0.8f;
        for (EdgeDraftGetter edge : container.getUnloader().getEdges()) {
        weightMin = Math.min(edge.getWeight(), weightMin);
        weightMax = Math.max(edge.getWeight(), weightMax);
        }
        if (weightMin == weightMax) {
        weightRatio = weightMinimum / weightMin;
        } else {
        weightRatio = Math.abs((weightMaximum - weightMinimum) / (weightMax - weightMin));
        }

        //Scale edge weight
        for (EdgeDraftGetter edge : container.getUnloader().getEdges()) {
        float weight = (edge.getWeight() - weightMin) * weightRatio + weightMinimum;
        assert !Float.isNaN(weight);
        edge.setWeight(weight);
        }*/
    }
}
