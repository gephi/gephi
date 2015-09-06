/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.columns;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.plugin.manipulators.columns.ui.ColumnValuesFrequencyUI;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulatorUI;
import org.gephi.utils.HTMLEscape;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsManipulator that shows a report with a list of the different values of a column and their frequency of appearance.
 * @author Eduardo Ramos
 */
//@ServiceProvider(service = AttributeColumnsManipulator.class)
public class ColumnValuesFrequency implements AttributeColumnsManipulator {

    public static final int MAX_PIE_CHART_CATEGORIES = 100;

    @Override
    public void execute(Table table, Column column) {
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.description");
    }

    @Override
    public boolean canManipulateColumn(Table table, Column column) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return ac.getTableRowsCount(table) > 0;//Make sure that there is at least 1 row
    }

    @Override
    public AttributeColumnsManipulatorUI getUI(Table table,Column column) {
        return new ColumnValuesFrequencyUI();
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalab/plugin/manipulators/resources/frequency-list.png");
    }

    public String getReportHTML(Table table, Column column, Map<Object, Integer> valuesFrequencies, JFreeChart pieChart, Dimension dimension) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        int totalValuesCount = ac.getTableRowsCount(table);
        ArrayList<Object> values = new ArrayList<Object>(valuesFrequencies.keySet());

        //Try to sort the values when they are comparable. (All objects of the set will have the same type) and not null:
        if (!values.isEmpty() && values.get(0) instanceof Comparable) {
            Collections.sort(values, new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    //Check for null objects because some comparables can't handle them (like Float...)
                    if (o1 == null) {
                        if (o2 == null) {
                            return 0;
                        } else {
                            return -1;//Null lesser than anything
                        }
                    } else if (o2 == null) {
                        if (o1 == null) {
                            return 0;
                        } else {
                            return 1;//Anything greater than null
                        }
                    } else {
                        return ((Comparable) o1).compareTo(o2);
                    }
                }
            });
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append(NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.report.header", HTMLEscape.stringToHTMLString(column.getTitle())));
        sb.append("<hr>");
        sb.append("<ol>");

        for (Object value : values) {
            writeValue(sb, value, valuesFrequencies, totalValuesCount);
        }
        sb.append("</ol>");
        sb.append("<hr>");

        if (!values.isEmpty() && values.size() <= MAX_PIE_CHART_CATEGORIES) {//Do not show pie chart if there are more than 100 different values
            try {
                if (pieChart != null) {
                    writePieChart(sb, pieChart, dimension);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            sb.append(NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.report.piechart.not-shown"));
        }

        sb.append("</html>");
        return sb.toString();
    }

    private void writeValue(final StringBuilder sb, final Object value, final Map<Object, Integer> valuesFrequencies, final float totalValuesCount) {
        int frequency = valuesFrequencies.get(value);

        sb.append("<li>");
        sb.append("<b>");
        if (value != null) {
            sb.append(HTMLEscape.stringToHTMLString(value.toString()));
        } else {
            sb.append("null");
        }
        sb.append("</b> - ");
        sb.append(frequency);
        sb.append(" (");
        sb.append(frequency / totalValuesCount * 100);
        sb.append("%");
        sb.append(" )");
        sb.append("</li>");
    }

    public Map<Object, Integer> buildValuesFrequencies(Table table, Column column){
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return ac.calculateColumnValuesFrequencies(table, column);
    }

    public JFreeChart buildPieChart(final Map<Object, Integer> valuesFrequencies) {
        final ArrayList<Object> values= new ArrayList<Object>(valuesFrequencies.keySet());
        DefaultPieDataset pieDataset = new DefaultPieDataset();

        for (Object value : values) {
            pieDataset.setValue(value != null ? "'" + value.toString() + "'" : "null", valuesFrequencies.get(value));
        }

        JFreeChart chart = ChartFactory.createPieChart(NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.report.piechart.title"), pieDataset, false, true, false);
        return chart;
    }

    private void writePieChart(final StringBuilder sb, JFreeChart chart, Dimension dimension) throws IOException {

        TempDir tempDir = TempDirUtils.createTempDir();
        String imageFile = "";
        String fileName = "frequencies-pie-chart.png";
        File file = tempDir.createFile(fileName);
        imageFile = "<center><img src=\"file:" + file.getAbsolutePath() + "\"</img></center>";
        ChartUtilities.saveChartAsPNG(file, chart, dimension != null ? dimension.width : 1000, dimension != null ? dimension.height : 1000);

        sb.append(imageFile);
    }
}
