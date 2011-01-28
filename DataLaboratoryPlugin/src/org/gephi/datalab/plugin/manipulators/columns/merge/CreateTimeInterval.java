/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalab.plugin.manipulators.columns.merge;

import java.text.SimpleDateFormat;
import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalab.plugin.manipulators.columns.merge.ui.CreateTimeIntervalUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsMergeStrategy for 1 or 2 columns that uses the column values as dates or numbers for start/end times
 * to create or fill the TimeInterval column for each row.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class CreateTimeInterval implements AttributeColumnsMergeStrategy {

    private AttributeTable table;
    private AttributeColumn[] columns;
    private AttributeColumn startColumn, endColumn;
    private boolean parseNumbers=true;
    //Number mode:
    private double startNumber, endNumber;
    //Date mode:
    private SimpleDateFormat dateFormat;
    private String startDate, endDate;

    public void setup(AttributeTable table, AttributeColumn[] columns) {
        this.table = table;
        this.columns = columns;
    }

    public void execute() {
        AttributeColumnsMergeStrategiesController ac=Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class);
        if(parseNumbers){
            ac.mergeNumericColumnsToTimeInterval(table, startColumn, endColumn, startNumber, endNumber);
        }else{
            ac.mergeDateColumnsToTimeInterval(table, startColumn, endColumn, dateFormat, startDate, endDate);
        }
    }

    public String getName() {
        return NbBundle.getMessage(CreateTimeInterval.class, "CreateTimeInterval.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(CreateTimeInterval.class, "CreateTimeInterval.description");
    }

    public boolean canExecute() {
        return columns.length == 1 || columns.length == 2;
    }

    public ManipulatorUI getUI() {
        return new CreateTimeIntervalUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 200;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/clock-select.png", true);
    }

    public AttributeColumn[] getColumns() {
        return columns;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public AttributeColumn getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(AttributeColumn endColumn) {
        this.endColumn = endColumn;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(double endNumber) {
        this.endNumber = endNumber;
    }

    public boolean isParseNumbers() {
        return parseNumbers;
    }

    public void setParseNumbers(boolean parseNumbers) {
        this.parseNumbers = parseNumbers;
    }

    public AttributeColumn getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(AttributeColumn startColumn) {
        this.startColumn = startColumn;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(double startNumber) {
        this.startNumber = startNumber;
    }
}
