/*
 Copyright 2008-2015 Gephi
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
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.plugin.manipulators.general.ui.ManageColumnEstimatorsUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.types.TimeMap;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * PluginGeneralActionsManipulator for managing column estimators.
 *
 * @author Eduardo Ramos
 */
@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class ManageColumnEstimators implements PluginGeneralActionsManipulator {

    private Column[] columns;
    private Estimator[] estimators;

    @Override
    public void execute() {
        for (int i = 0; i < columns.length; i++) {
            Column column = columns[i];
            Estimator estimator = estimators[i];
            
            column.setEstimator(estimator);
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ManageColumnEstimators.class, "ManageColumnEstimators.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ManageColumnEstimators.class, "ManageColumnEstimators.description");
    }
    
    public List<Column> getColumns(){
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        
        Table table;
        if(Lookup.getDefault().lookup(DataTablesController.class).isNodeTableMode()){
            table = graphModel.getNodeTable();
        } else {
            table = graphModel.getEdgeTable();
        }
        
        List<Column> availableColumns = new ArrayList<>();
        for (Column column : table) {
            if(TimeMap.class.isAssignableFrom(column.getTypeClass())){
                availableColumns.add(column);
            }
        }
        return availableColumns;
    }

    @Override
    public boolean canExecute() {
        return !getColumns().isEmpty();
    }

    @Override
    public ManipulatorUI getUI() {
        this.columns = null;
        this.estimators = null;
        return new ManageColumnEstimatorsUI();
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/gear.png", true);
    }

    public void setup(Column[] columns, Estimator[] estimators) {
        this.columns = columns;
        this.estimators = estimators;
    }
}
