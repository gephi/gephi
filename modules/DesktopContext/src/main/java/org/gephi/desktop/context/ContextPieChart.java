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
package org.gephi.desktop.context;

import java.awt.Color;
import java.awt.FlowLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ContextPieChart {

    private DefaultPieDataset data;
    private ChartPanel chartPanel;

    public ContextPieChart() {
        data = new DefaultPieDataset();
        final JFreeChart chart = ChartFactory.createPieChart("Employee Survey", data, false, false, false);
        chart.setTitle(new TextTitle());
        chart.setBackgroundPaint(null);
        chart.setPadding(new RectangleInsets(0, 0, 0, 0));
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setSimpleLabels(true);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlineStroke(null);
        plot.setLabelShadowPaint(null);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new java.awt.Font("Tahoma", 0, 10));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelGap(0.5);
        plot.setCircular(true);
        plot.setInteriorGap(0);
        plot.setBackgroundPaint(null);
        plot.setBackgroundAlpha(1f);
        plot.setSectionPaint(NbBundle.getMessage(getClass(), "ContextPieChart.visible"), new Color(0x222222));
        plot.setSectionPaint(NbBundle.getMessage(getClass(), "ContextPieChart.notVisible"), new Color(0xDDDDDD));
        chartPanel = new ChartPanel(chart, 100, 100, 10, 10, 300, 300, true, false, false, false, false, false);
        ((FlowLayout) chartPanel.getLayout()).setHgap(0);
        ((FlowLayout) chartPanel.getLayout()).setVgap(0);
        chartPanel.setOpaque(false);
        chartPanel.setPopupMenu(null);
    }

    public void refreshChart(double visiblePercentage) {
        data.setValue(NbBundle.getMessage(getClass(), "ContextPieChart.visible"), visiblePercentage);
        data.setValue(NbBundle.getMessage(getClass(), "ContextPieChart.notVisible"), 1 - visiblePercentage);
    }

    public void setChartVisible(boolean visible) {
        chartPanel.setVisible(visible);
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
