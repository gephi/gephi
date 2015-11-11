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
package org.gephi.datalab.plugin.manipulators.general;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.plugin.manipulators.general.ui.MergeNodeDuplicatesUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * PluginGeneralActionsManipulator that automatically detects and merges node duplicates based on a column
 * @author Eduardo Ramos
 */
@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class MergeNodeDuplicates implements PluginGeneralActionsManipulator {

    public static final String DELETE_MERGED_NODES_SAVED_PREFERENCES = "MergeNodeDuplicates_DeleteMergedNodes";
    public static final String CASE_SENSITIVE_SAVED_PREFERENCES = "MergeNodeDuplicates_CaseSensitive";
    /**
     * To be set by the UI
     */
    private List<List<Node>> duplicateGroups;
    private boolean deleteMergedNodes;
    private boolean caseSensitive;
    private Column[] columns;
    private AttributeRowsMergeStrategy[] mergeStrategies;

    @Override
    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        for (List<Node> nodes : duplicateGroups) {
            gec.mergeNodes(nodes.toArray(new Node[0]), nodes.get(0), columns, mergeStrategies, deleteMergedNodes);
        }
        NbPreferences.forModule(MergeNodeDuplicates.class).putBoolean(DELETE_MERGED_NODES_SAVED_PREFERENCES, deleteMergedNodes);
        NbPreferences.forModule(MergeNodeDuplicates.class).putBoolean(CASE_SENSITIVE_SAVED_PREFERENCES, caseSensitive);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MergeNodeDuplicates.class, "MergeNodeDuplicates.name");
    }

    @Override
    public String getDescription() {
        return "MergeNodeDuplicates.description";
    }

    @Override
    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).getNodesCount() > 0;
    }

    @Override
    public ManipulatorUI getUI() {
        Table nodeTable = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
        List<Column> columnsList = new ArrayList<Column>();
        for (Column column : nodeTable) {
            if(!column.isReadOnly()){
                columnsList.add(column);
            }
        }
        
        columns = columnsList.toArray(new Column[0]);
        mergeStrategies = new AttributeRowsMergeStrategy[columns.length];
        deleteMergedNodes = NbPreferences.forModule(MergeNodeDuplicates.class).getBoolean(DELETE_MERGED_NODES_SAVED_PREFERENCES, true);
        caseSensitive = NbPreferences.forModule(MergeNodeDuplicates.class).getBoolean(CASE_SENSITIVE_SAVED_PREFERENCES, true);
        return new MergeNodeDuplicatesUI();
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/merge.png", true);
    }

    public Column[] getColumns() {
        return columns;
    }

    public void setColumns(Column[] columns) {
        this.columns = columns;
    }

    public boolean isDeleteMergedNodes() {
        return deleteMergedNodes;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setDeleteMergedNodes(boolean deleteMergedNodes) {
        this.deleteMergedNodes = deleteMergedNodes;
    }

    public List<List<Node>> getDuplicateGroups() {
        return duplicateGroups;
    }

    public void setDuplicateGroups(List<List<Node>> duplicateGroups) {
        this.duplicateGroups = duplicateGroups;
    }

    public AttributeRowsMergeStrategy[] getMergeStrategies() {
        return mergeStrategies;
    }

    public void setMergeStrategies(AttributeRowsMergeStrategy[] mergeStrategies) {
        this.mergeStrategies = mergeStrategies;
    }
}
