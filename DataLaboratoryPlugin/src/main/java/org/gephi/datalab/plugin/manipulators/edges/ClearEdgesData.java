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
package org.gephi.datalab.plugin.manipulators.edges;

import java.util.ArrayList;
import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.plugin.manipulators.GeneralColumnsChooser;
import org.gephi.datalab.plugin.manipulators.ui.GeneralChooseColumnsUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that clears the given columns data of one or more edges except the id and computed attributes.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ClearEdgesData extends BasicEdgesManipulator implements GeneralColumnsChooser {

    private Edge[] edges;
    private AttributeColumn[] columnsToClearData;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.edges = edges;
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<AttributeColumn> columnsToClearDataList = new ArrayList<AttributeColumn>();
        for (AttributeColumn column : Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable().getColumns()) {
            if (ac.canClearColumnData(column)) {
                columnsToClearDataList.add(column);
            }
        }
        columnsToClearData = columnsToClearDataList.toArray(new AttributeColumn[0]);
    }

    public void execute() {
        if (columnsToClearData.length >= 0) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.clearEdgesData(edges, columnsToClearData);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        if (edges.length > 1) {
            return NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.name.multiple");
        } else {
            return NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.name.single");
        }
    }

    public String getDescription() {
        return NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.description");
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return new GeneralChooseColumnsUI(NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.ui.description"));
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/clear-data.png", true);
    }

    public AttributeColumn[] getColumns() {
        return columnsToClearData;
    }

    public void setColumns(AttributeColumn[] columnsToClearData) {
        this.columnsToClearData = columnsToClearData;
    }
}
