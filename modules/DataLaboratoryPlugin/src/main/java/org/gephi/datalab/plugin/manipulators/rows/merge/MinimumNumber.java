/*
Copyright 2008-2011 Gephi
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
package org.gephi.datalab.plugin.manipulators.rows.merge;

import java.math.BigDecimal;
import javax.swing.Icon;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.utils.StatisticsUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeRowsMergeStrategy for any number or number list column that
 * calculates the minimum of all the values.
 * @author Eduardo Ramos
 */
public class MinimumNumber implements AttributeRowsMergeStrategy {

    private Element[] rows;
    private Column column;
    private BigDecimal result;

    @Override
    public void setup(Element[] rows, Element selectedRow, Column column) {
        this.rows = rows;
        this.column = column;
    }

    @Override
    public Object getReducedValue() {
        return result;
    }

    @Override
    public void execute() {
        result = StatisticsUtils.minValue(Lookup.getDefault().lookup(AttributeColumnsController.class).getRowsColumnNumbers(rows, column));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AverageNumber.class, "MinimumNumber.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AverageNumber.class, "MinimumNumber.description");
    }

    @Override
    public boolean canExecute() {
        return AttributeUtils.isNumberType(column.getTypeClass());
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 600;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/minus-white.png", true);
    }
}
