/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.desktop.datalab.tables;

import org.gephi.desktop.datalab.tables.columns.ElementDataColumn;
import org.gephi.desktop.datalab.tables.columns.PropertyDataColumn;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.desktop.datalab.tables.popup.EdgesPopupAdapter;
import org.gephi.graph.api.Edge;
import org.gephi.tools.api.EditWindowController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Eduardo Ramos
 */
public final class EdgesDataTable extends AbstractElementsDataTable<Edge> {

    public EdgesDataTable() {
        super();
        
        //Add listener of table selection to refresh edit window when the selection changes (and if the table is not being refreshed):
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!isRefreshingTable()) {
                    EditWindowController edc = Lookup.getDefault().lookup(EditWindowController.class);
                    if (edc != null && edc.isOpen()) {
                        if (table.getSelectedRow() != -1) {
                            edc.editEdges(getElementsFromSelectedRows().toArray(new Edge[0]));
                        } else {
                            edc.disableEdit();
                        }
                    }
                }
            }
        });
        
        
        
        table.addMouseListener(new EdgesPopupAdapter(this));
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
                    List<Edge> selectedEdges = getElementsFromSelectedRows();
                    if (!selectedEdges.isEmpty()) {
                        EdgesManipulator del = dlh.getEdgesManipulatorByName("DeleteEdges");
                        if (del != null) {
                            del.setup(selectedEdges.toArray(new Edge[0]), null);
                            if (del.canExecute()) {
                                dlh.executeManipulator(del);
                            }
                        }
                    }
                }
            }
        });
    }
    
    

    private List<PropertyDataColumn<Edge>> propertiesColumns;
    private boolean showEdgesNodesLabels = false;

    @Override
    public List<? extends ElementDataColumn<Edge>> getFakeDataColumns() {
        if(propertiesColumns != null){
            return propertiesColumns;
        }
        
        propertiesColumns = new ArrayList<PropertyDataColumn<Edge>>();

        propertiesColumns.add(new PropertyDataColumn<Edge>(NbBundle.getMessage(EdgesDataTable.class, "EdgeDataTable.source.column.text")) {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(Edge edge) {
                if (showEdgesNodesLabels) {
                    return edge.getSource().getId() + " - " + edge.getSource().getLabel();
                } else {
                    return edge.getSource().getId();
                }
            }
        });

        propertiesColumns.add(new PropertyDataColumn<Edge>(NbBundle.getMessage(EdgesDataTable.class, "EdgeDataTable.target.column.text")) {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(Edge edge) {
                if (showEdgesNodesLabels) {
                    return edge.getTarget().getId() + " - " + edge.getTarget().getLabel();
                } else {
                    return edge.getTarget().getId();
                }
            }
        });
        
        propertiesColumns.add(new PropertyDataColumn<Edge>(NbBundle.getMessage(EdgesDataTable.class, "EdgeDataTable.type.column.text")) {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(Edge edge) {
                if (edge.isDirected()) {
                    return NbBundle.getMessage(EdgesDataTable.class, "EdgeDataTable.type.column.directed");
                } else {
                    return NbBundle.getMessage(EdgesDataTable.class, "EdgeDataTable.type.column.undirected");
                }
            }
        });

        return propertiesColumns;
    }
    
    public boolean isShowEdgesNodesLabels() {
        return showEdgesNodesLabels;
    }

    public void setShowEdgesNodesLabels(boolean showEdgesNodesLabels) {
        this.showEdgesNodesLabels = showEdgesNodesLabels;
    }
}
