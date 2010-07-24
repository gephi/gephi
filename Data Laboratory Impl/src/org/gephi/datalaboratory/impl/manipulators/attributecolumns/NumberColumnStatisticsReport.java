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

import java.awt.Image;
import java.math.BigDecimal;
import javax.swing.SwingUtilities;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.utils.HTMLEscape;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.gephi.ui.components.SimpleHTMLReport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * AttributeColumnsManipulator that shows a report with statistics values of a number/number list column.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class NumberColumnStatisticsReport implements AttributeColumnsManipulator {

    public void execute(AttributeTable table, AttributeColumn column) {
        final BigDecimal[] statistics=Lookup.getDefault().lookup(AttributeColumnsController.class).getNumberOrNumberListColumnStatistics(table, column);
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(NbBundle.getMessage(NumberColumnStatisticsReport.class, "NumberColumnStatisticsReport.report.header",HTMLEscape.stringToHTMLString(column.getTitle())));
        sb.append("<hr>");
        writeStatistic(sb,"NumberColumnStatisticsReport.report.average",statistics[0]);
        writeStatistic(sb,"NumberColumnStatisticsReport.report.median",statistics[1]);
        writeStatistic(sb,"NumberColumnStatisticsReport.report.sum",statistics[2]);
        writeStatistic(sb,"NumberColumnStatisticsReport.report.min",statistics[3]);
        writeStatistic(sb,"NumberColumnStatisticsReport.report.max",statistics[4]);
        sb.append("<ul>");
        sb.append("</ul></html>");

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                SimpleHTMLReport dialog = new SimpleHTMLReport(WindowManager.getDefault().getMainWindow(), sb.toString());
            }
        });
    }

    public String getName() {
        return getMessage("NumberColumnStatisticsReport.name");
    }

    public String getDescription() {
        return getMessage("NumberColumnStatisticsReport.description");
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return AttributeUtils.getDefault().isNumberOrNumberListColumn(column) && ac.getTableRowsCount(table) > 0;//Make sure it is a number/number list column and there is at least 1 row
    }

    public AttributeColumnsManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 100;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/statistics.png");
    }

    private void writeStatistic(StringBuilder sb, String resName, BigDecimal number){
        sb.append("<li>");
        sb.append(getMessage(resName));
        sb.append(": ");
        sb.append(number);
        sb.append("</li>");
    }

    private String getMessage(String resName){
        return NbBundle.getMessage(NumberColumnStatisticsReport.class, resName);
    }
}
