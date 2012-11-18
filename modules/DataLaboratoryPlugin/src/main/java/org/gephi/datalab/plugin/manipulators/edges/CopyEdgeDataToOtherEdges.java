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
import org.gephi.datalab.plugin.manipulators.GeneralColumnsAndRowChooser;
import org.gephi.datalab.plugin.manipulators.ui.GeneralChooseColumnsAndRowUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that copies the given columns data of one edge to the other selected edges.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class CopyEdgeDataToOtherEdges extends BasicEdgesManipulator implements GeneralColumnsAndRowChooser {

    private Edge clickedEdge;
    private Edge[] edges;
    private AttributeColumn[] columnsToCopyData;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.clickedEdge = clickedEdge;
        this.edges = edges;
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<AttributeColumn> columnsToCopyDataList = new ArrayList<AttributeColumn>();
        for (AttributeColumn column : Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable().getColumns()) {
            if (ac.canChangeColumnData(column)) {
                columnsToCopyDataList.add(column);
            }
        }
        columnsToCopyData = columnsToCopyDataList.toArray(new AttributeColumn[0]);
    }

    public void execute() {
        if (columnsToCopyData.length >= 0) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.copyEdgeDataToOtherEdges(clickedEdge, edges, columnsToCopyData);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        return NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.description");
    }

    public boolean canExecute() {
        return edges.length>1;//At least 2 edges to copy data from one to the other.
    }

    public ManipulatorUI getUI() {
        return new GeneralChooseColumnsAndRowUI(NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.ui.rowDescription"),NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.ui.columnsDescription"));
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 200;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/broom--arrow.png", true);
    }

    public AttributeColumn[] getColumns() {
        return columnsToCopyData;
    }

    public void setColumns(AttributeColumn[] columnsToClearData) {
        this.columnsToCopyData = columnsToClearData;
    }

    public Object[] getRows() {
        return edges;
    }

    public Object getRow() {
        return clickedEdge;
    }

    public void setRow(Object row) {
        clickedEdge=(Edge) row;
    }
}
