/*
 Copyright 2008-2010 Gephi
 Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.scale;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Sample layout that scales the graph.
 *
 * @author Helder Suzuki
 */
public abstract class AbstractScaleLayout extends AbstractLayout implements Layout {

    private double scale;
    private Graph graph;

    public AbstractScaleLayout(LayoutBuilder layoutBuilder, double scale) {
        super(layoutBuilder);
        this.scale = scale;
    }

    @Override
    public void initAlgo() {
        setConverged(false);
    }

    @Override
    public void goAlgo() {
        graph = graphModel.getGraphVisible();
        graph.readLock();
        try {
            double xMean = 0, yMean = 0;
            for (Node n : graph.getNodes()) {
                xMean += n.x();
                yMean += n.y();
            }
            xMean /= graph.getNodeCount();
            yMean /= graph.getNodeCount();

            for (Node n : graph.getNodes()) {
                double dx = (n.x() - xMean) * getScale();
                double dy = (n.y() - yMean) * getScale();

                n.setX((float) (xMean + dx));
                n.setY((float) (yMean + dy));
            }
            setConverged(true);
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ScaleLayout.scaleFactor.name"),
                    null,
                    "ScaleLayout.scaleFactor.name",
                    NbBundle.getMessage(getClass(), "ScaleLayout.scaleFactor.desc"),
                    "getScale", "setScale"));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
    }

    /**
     * @return the scale
     */
    public Double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }
}
