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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.impl.manipulators.ui.GeneralNumberListStatisticsReportUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributevalues.AttributeValueManipulator;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeValueManipulator that shows a report with statistics values and charts of a dynamic number/number list AttributeValue.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class NumberListStatisticsReport implements AttributeValueManipulator {

    private Number[] numbers;
    private AttributeColumn column;

    public void setup(AttributeRow row, AttributeColumn column) {
        this.column = column;
        AttributeUtils attributeUtils = AttributeUtils.getDefault();
        if (attributeUtils.isNumberListColumn(column) || attributeUtils.isDynamicNumberColumn(column)) {
            numbers = Lookup.getDefault().lookup(AttributeColumnsController.class).getRowNumbers(row, new AttributeColumn[]{column});
        }
    }

    public void execute() {
    }

    public String getName() {
        return getMessage("NumberListStatisticsReport.name");
    }

    public String getDescription() {
        return getMessage("NumberListStatisticsReport.description");
    }

    public boolean canExecute() {
        return numbers != null && numbers.length > 1;//Column is number list column and there is numbers to show
    }

    public ManipulatorUI getUI() {
        return new GeneralNumberListStatisticsReportUI(numbers, column.getTitle(), getName());
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

    private String getMessage(String resName) {
        return NbBundle.getMessage(NumberListStatisticsReport.class, resName);
    }
}
