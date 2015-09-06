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

import javax.swing.Icon;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalab.plugin.manipulators.columns.merge.ui.GeneralColumnTitleChooserUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsMergeStrategy for any combination of number or number list columns that
 * calculates the maximum value of all the values and creates a new BigDecimal column with the result of each row.
 * @author Eduardo Ramos
 */
public class MaximumNumber implements AttributeColumnsMergeStrategy, GeneralColumnTitleChooser {

    private Table table;
    private Column[] columns;
    private String columnTitle;

    @Override
    public void setup(Table table, Column[] columns) {
        this.table = table;
        this.columns = columns;
    }

    @Override
    public void execute() {
        Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class).maxValueNumbersMerge(table, columns, columnTitle);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MaximumNumber.class, "MaximumNumber.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MaximumNumber.class, "MaximumNumber.description");
    }

    @Override
    public boolean canExecute() {
        for (Column column : columns) {
            if(!AttributeUtils.isNumberType(column.getTypeClass())){
                return false;
            }
        }
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new GeneralColumnTitleChooserUI();
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 700;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/plus-white.png", true);
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public String getColumnTitle() {
        return columnTitle;
    }

    @Override
    public void setColumnTitle(String columnTitle) {
        this.columnTitle = columnTitle;
    }
}
