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
