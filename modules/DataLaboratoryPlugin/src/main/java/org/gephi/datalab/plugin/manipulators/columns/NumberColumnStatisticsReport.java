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

import java.awt.Image;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.plugin.manipulators.ui.GeneralNumberListStatisticsReportUI;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulatorUI;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsManipulator that shows a report with statistics values and charts of a number/number list column.
 * @author Eduardo Ramos
 */
//@ServiceProvider(service = AttributeColumnsManipulator.class)
public class NumberColumnStatisticsReport implements AttributeColumnsManipulator {

    @Override
    public void execute(Table table, Column column) {
    }

    @Override
    public String getName() {
        return getMessage("NumberColumnStatisticsReport.name");
    }

    @Override
    public String getDescription() {
        return getMessage("NumberColumnStatisticsReport.description");
    }

    @Override
    public boolean canManipulateColumn(Table table, Column column) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return AttributeUtils.isNumberType(column.getTypeClass()) && ac.getTableRowsCount(table) > 0;//Make sure it is a number/number list column and there is at least 1 row
    }

    @Override
    public AttributeColumnsManipulatorUI getUI(Table table,Column column) {
        return new GeneralNumberListStatisticsReportUI(getColumnNumbers(table, column), column.getTitle(), getName());
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalab/plugin/manipulators/resources/statistics.png");
    }

    public Number[] getColumnNumbers(final Table table, final Column column) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Number[] columnNumbers = ac.getColumnNumbers(table, column);
        return columnNumbers;
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(NumberColumnStatisticsReport.class, resName);
    }
}
