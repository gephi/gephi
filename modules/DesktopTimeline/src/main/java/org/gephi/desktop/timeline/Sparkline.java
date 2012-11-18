/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian
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
package org.gephi.desktop.timeline;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.gephi.timeline.api.TimelineChart;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.utils.sparklines.SparklineGraph;
import org.gephi.utils.sparklines.SparklineParameters;

/**
 *
 * @author Mathieu Bastian
 */
public class Sparkline {

    private double min;
    private double max;
    private SparklineParameters parameters;
    private TimelineChart chart;
    private BufferedImage image;

    public BufferedImage getImage(TimelineModel model, int width, int height) {
        double newMin = model.getCustomMin();
        double newMax = model.getCustomMax();
        TimelineChart newChart = model.getChart();
        if (chart == null || newMax != max || newMin != min || image.getWidth() != width || image.getHeight() != height
                || newChart != chart) {
            min = newMin;
            max = newMax;
            chart = newChart;

            if (chart != null) {
                double minX = chart.getMinX().doubleValue();
                double maxX = chart.getMaxX().doubleValue();
                int sparklineWidth = (int) (((maxX - minX) / (max - min)) * width);

                parameters = new SparklineParameters(sparklineWidth, height);
                parameters.setTransparentBackground(true);
                parameters.setDrawArea(true);

                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                int sparklineX = (int) ((minX - min) / (max - min) * width);
                BufferedImage sparklineImage = draw();
                Graphics g = image.getGraphics();
                g.drawImage(sparklineImage, sparklineX, 0, null);
                g.dispose();
            } else {
                return null;
            }
        }
        return image;
    }

    private BufferedImage draw() {
        BufferedImage img = SparklineGraph.draw(chart.getX(), chart.getY(), chart.getMinY(), chart.getMaxY(), parameters);
        return img;
    }
}
