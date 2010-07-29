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
package org.gephi.datalaboratory.impl.manipulators.attributevalues;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.utils.ChartsBuilder;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributevalues.AttributeValueManipulator;
import org.gephi.ui.components.JFreeChartDialog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * AttributeValueManipulator that shows a scatter plot with the numbers of a number list column cell.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ShowNumberListScatterPlot implements AttributeValueManipulator {

    private Number[] numbers;
    private AttributeColumn column;

    public void setup(AttributeRow row, AttributeColumn column) {
        this.column = column;
        AttributeUtils attributeUtils = AttributeUtils.getDefault();
        if (attributeUtils.isNumberListColumn(column)) {
            numbers = Lookup.getDefault().lookup(AttributeColumnsController.class).getRowNumbers(row, new AttributeColumn[]{column});
        } else if (attributeUtils.isDynamicNumberColumn(column)) {
            DynamicType dynamicList = (DynamicType) row.getValue(column.getIndex());
            if (dynamicList != null) {
                numbers = (Number[]) dynamicList.getValues().toArray(new Number[0]);
            }
        }
    }

    public void execute() {
        final JFreeChart scatterPlot = buildScatterPlot(numbers, column.getTitle());
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFreeChartDialog scatterPlotDialog = new JFreeChartDialog(WindowManager.getDefault().getMainWindow(), scatterPlot.getTitle().getText(), scatterPlot, 600, 400);
            }
        });

    }

    public String getName() {
        return NbBundle.getMessage(ShowNumberListScatterPlot.class, "ShowNumberListScatterPlot.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(ShowNumberListScatterPlot.class, "ShowNumberListScatterPlot.description");
    }

    public boolean canExecute() {
        return numbers != null && numbers.length > 1;//Column is number list column and there is numbers to show
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/chart-up.png", true);
    }

    private JFreeChart buildScatterPlot(final Number[] numbers, final String columnTitle) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series = new XYSeries(columnTitle);
        for (int i = 0; i < numbers.length; i++) {
            series.add(i, numbers[i]);
        }
        dataset.addSeries(series);
        JFreeChart scatterPlot = ChartsBuilder.buildScatterPlot(dataset,
                getMessage("ShowNumberListScatterPlot.scatter-plot.title"),
                getMessage("ShowNumberListScatterPlot.scatter-plot.xLabel"),
                columnTitle,
                true,
                false);

        return scatterPlot;
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(ShowNumberListScatterPlot.class, resName);
    }
}
