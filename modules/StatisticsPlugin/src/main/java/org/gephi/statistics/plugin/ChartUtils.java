/*
Copyright 2011 Gephi
Authors : Sebastien Heymann <seb@gephi.org>
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

package org.gephi.statistics.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author sebastien
 */
public abstract class ChartUtils {

    public static void decorateChart(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(0, 0, 2, 2));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);
        plot.setRenderer(renderer);
    }

    public static void scaleChart(JFreeChart chart, XYSeries dSeries, boolean normalized) {
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLowerMargin(1.0);
        domainAxis.setUpperMargin(1.0);
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (normalized) {
            domainAxis.setRange(-0.05, 1.05);
        } else {
            domainAxis.setRange(dSeries.getMinX() - 1, dSeries.getMaxX() + 1);
        }
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(-0.1 * Math.sqrt(dSeries.getMaxY()), dSeries.getMaxY() + 0.1 * Math.sqrt(dSeries.getMaxY()));
    }

    public static String renderChart(JFreeChart chart, String fileName) {
        String imageFile = "";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            TempDir tempDir = TempDirUtils.createTempDir();
            File file1 = tempDir.createFile(fileName);
            imageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);
        } catch (IOException e) {
        }
        return imageFile;
    }

    public static XYSeries createXYSeries(Map data, String name) {
        XYSeries series = new XYSeries(name);
        for (Iterator it = data.entrySet().iterator(); it.hasNext();) {
            Map.Entry d = (Map.Entry) it.next();
            Number x = (Number) d.getKey();
            Number y = (Number) d.getValue();
            series.add(x, y);
        }
        return series;
    }

}
