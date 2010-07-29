/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.attributecolumns;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.utils.HTMLEscape;
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.ui.ColumnValuesFrequencyUI;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
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
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeColumnsManipulator that shows a report with a list of the different values of a column and their frequency of appearance.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class ColumnValuesFrequency implements AttributeColumnsManipulator {

    public static final int MAX_PIE_CHART_CATEGORIES = 100;

    public void execute(AttributeTable table, AttributeColumn column) {
    }

    public String getName() {
        return NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(ColumnValuesFrequency.class, "ColumnValuesFrequency.description");
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return ac.getTableRowsCount(table) > 0;//Make sure that there is at least 1 row
    }

    public AttributeColumnsManipulatorUI getUI() {
        return new ColumnValuesFrequencyUI();
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 0;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/frequency-list.png");
    }

    public String getReportHTML(AttributeTable table, AttributeColumn column, Map<Object, Integer> valuesFrequencies, JFreeChart pieChart, Dimension dimension) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        int totalValuesCount = ac.getTableRowsCount(table);
        ArrayList<Object> values = new ArrayList<Object>(valuesFrequencies.keySet());

        //Try to sort the values when they are comparable. (All objects of the set will have the same type) and not null:
        if (!values.isEmpty() && values.get(0) instanceof Comparable) {
            Collections.sort(values, new Comparator<Object>() {

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

    public Map<Object, Integer> buildValuesFrequencies(AttributeTable table, AttributeColumn column){
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
