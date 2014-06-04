/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.io.processor.plugin;

import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.NodeDraft;
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

    @Override
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

        //Recenter
        double centroidX = 0;
        double centroidY = 0;
        int nodeSize = 0;
        for (NodeDraft node : container.getUnloader().getNodes()) {
            centroidX += node.getX();
            centroidY += node.getY();
            nodeSize++;
        }
        centroidX /= nodeSize;
        centroidY /= nodeSize;
        for (NodeDraft node : container.getUnloader().getNodes()) {
            node.setX((float) (node.getX() - centroidX));
            node.setY((float) (node.getY() - centroidY));
        }

        //Measure
        for (NodeDraft node : container.getUnloader().getNodes()) {
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
                sizeRatio = Math.abs(octreeLimit / xMin);
            }
            if (xMax * sizeRatio > octreeLimit) {
                sizeRatio = Math.abs(octreeLimit / xMax);
            }
            if (yMin * sizeRatio < -octreeLimit) {
                sizeRatio = Math.abs(octreeLimit / yMin);
            }
            if (yMax * sizeRatio > octreeLimit) {
                sizeRatio = Math.abs(octreeLimit / yMax);
            }
            if (zMin * sizeRatio < -octreeLimit) {
                sizeRatio = Math.abs(octreeLimit / zMin);
            }
            if (zMax * sizeRatio > octreeLimit) {
                sizeRatio = Math.abs(octreeLimit / zMax);
            }

            averageSize = 0f;

            //Scale node size
            for (NodeDraft node : container.getUnloader().getNodes()) {
                float size = (node.getSize() - sizeMin) * sizeRatio + sizeMinimum;
                node.setSize(size);
                node.setX(node.getX() * sizeRatio);
                node.setY(node.getY() * sizeRatio);
                node.setZ(node.getZ() * sizeRatio);
                averageSize += size;
            }
            averageSize /= nodeSize;
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
