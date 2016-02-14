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
package org.gephi.datalab.plugin.manipulators.nodes;

import java.util.ArrayList;
import javax.swing.Icon;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.plugin.manipulators.GeneralColumnsAndRowChooser;
import org.gephi.datalab.plugin.manipulators.ui.GeneralChooseColumnsAndRowUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that copies the given columns data of one node to the other selected nodes.
 * @author Eduardo Ramos
 */
public class CopyNodeDataToOtherNodes extends BasicNodesManipulator implements GeneralColumnsAndRowChooser {

    private Node clickedNode;
    private Node[] nodes;
    private Column[] columnsToCopyData;

    @Override
    public void setup(Node[] nodes, Node clickedNode) {
        this.clickedNode = clickedNode;
        this.nodes = nodes;
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<Column> columnsToCopyDataList = new ArrayList<>();
        for (Column column : Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable()) {
            if (ac.canChangeColumnData(column)) {
                columnsToCopyDataList.add(column);
            }
        }
        columnsToCopyData = columnsToCopyDataList.toArray(new Column[0]);
    }

    @Override
    public void execute() {
        if (columnsToCopyData.length >= 0) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.copyNodeDataToOtherNodes(clickedNode, nodes, columnsToCopyData);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CopyNodeDataToOtherNodes.class, "CopyNodeDataToOtherNodes.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(CopyNodeDataToOtherNodes.class, "CopyNodeDataToOtherNodes.description");
    }

    @Override
    public boolean canExecute() {
        return nodes.length > 1;//At least 2 nodes to copy data from one to the other.
    }

    @Override
    public ManipulatorUI getUI() {
        return new GeneralChooseColumnsAndRowUI(NbBundle.getMessage(CopyNodeDataToOtherNodes.class, "CopyNodeDataToOtherNodes.ui.rowDescription"),NbBundle.getMessage(CopyNodeDataToOtherNodes.class, "CopyNodeDataToOtherNodes.ui.columnsDescription"));
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 200;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/broom--arrow.png", true);
    }

    @Override
    public Column[] getColumns() {
        return columnsToCopyData;
    }

    @Override
    public void setColumns(Column[] columnsToClearData) {
        this.columnsToCopyData = columnsToClearData;
    }

    @Override
    public Element[] getRows() {
        return nodes;
    }

    @Override
    public Element getRow() {
        return clickedNode;
    }

    @Override
    public void setRow(Element row) {
        clickedNode=(Node) row;
    }
}
