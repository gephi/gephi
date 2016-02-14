/*
Copyright 2008-2016 Gephi
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
package org.gephi.desktop.datalab;

import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.TableDiff;
import org.gephi.graph.api.TableObserver;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 * Class for managing observers of a workspace to automatically refresh data tables.
 * @author Eduardo Ramos
 */
public class DataTablesObservers {
    private final Workspace workspace;
    private final GraphModel graphModel;
    
    private GraphObserver graphObserver;
    private TableObserver nodesTableObserver;
    private TableObserver edgesTableObserver;
    private final Set<ColumnObserver> columnObservers;

    public DataTablesObservers(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.columnObservers = new HashSet<>();
    }
    
    public synchronized void initialize(){
        if(graphObserver != null){
            return;
        }
        
        graphObserver = graphModel.createGraphObserver(graphModel.getGraph(), false);
        nodesTableObserver = graphModel.getNodeTable().createTableObserver(true);
        edgesTableObserver = graphModel.getEdgeTable().createTableObserver(true);
        
        for (Column column : graphModel.getNodeTable()) {
            createColumnObserver(column);
        }
        for (Column column : graphModel.getEdgeTable()){
            createColumnObserver(column);
        }
    }
    
    public synchronized void destroy(){
        if(graphObserver != null){
            graphObserver.destroy();
            graphObserver = null;
        }
        if(nodesTableObserver != null){
            nodesTableObserver.destroy();
            nodesTableObserver = null;
        }
        if(edgesTableObserver != null){
            edgesTableObserver.destroy();
            edgesTableObserver = null;
        }
        
        for (ColumnObserver columnObserver : columnObservers) {
            columnObserver.destroy();
        }
        columnObservers.clear();
    }
    
    public boolean hasChanges(){
        if(graphObserver == null){
            return false;//Not initialized
        }
        
        boolean hasChanges = graphObserver.hasGraphChanged();
        hasChanges = processTableObseverChanges(nodesTableObserver) || hasChanges;
        hasChanges = processTableObseverChanges(edgesTableObserver) || hasChanges;
        
        LatestVisibleView latestVisibleView = workspace.getLookup().lookup(LatestVisibleView.class);

        if(latestVisibleView != null){
            workspace.remove(latestVisibleView);
            if(latestVisibleView.getView().isDestroyed()){
                hasChanges = true;
            }
        }
        workspace.add(new LatestVisibleView(graphModel.getVisibleView()));
        
        if(!hasChanges){
            for (ColumnObserver columnObserver : columnObservers) {
                if(columnObserver.hasColumnChanged()){
                    hasChanges = true;
                    break;
                }
            }
        }
        
        return hasChanges;
    }
    
    private boolean processTableObseverChanges(TableObserver observer){
        boolean hasChanges = false;
        if(observer.hasTableChanged()){
            hasChanges = true;
            TableDiff diff = observer.getDiff();
            for (Column addedColumn : diff.getAddedColumns()) {
                createColumnObserver(addedColumn);
            }
            for (Column removedColumn : diff.getRemovedColumns()) {
                for (ColumnObserver columnObserver : columnObservers.toArray(new ColumnObserver[0])) {
                    if(columnObserver.getColumn() == removedColumn){
                        columnObserver.destroy();
                        columnObservers.remove(columnObserver);
                    }
                }
            }
        }
        
        return hasChanges;
    }

    private void createColumnObserver(Column column) {
        ColumnObserver observer = column.createColumnObserver(false);
        columnObservers.add(observer);
    }
    
    /**
     * Used only for detecting changes in graph filters (visible view).
     */
    class LatestVisibleView {
        private final GraphView view;

        public LatestVisibleView(GraphView view) {
            this.view = view;
        }

        public GraphView getView() {
            return view;
        }
    }
}
