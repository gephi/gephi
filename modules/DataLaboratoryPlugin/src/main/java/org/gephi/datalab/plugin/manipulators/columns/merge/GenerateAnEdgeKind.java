package org.gephi.datalab.plugin.manipulators.columns.merge;

import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Table;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

import javax.swing.*;

public class GenerateAnEdgeKind implements AttributeColumnsMergeStrategy {




    private Table table;
    private Column[] columns;
    @Override
    public void setup(Table table, Column[] columns) {
    this.table = table;
    this.columns=columns;
    }

    @Override
    public void execute() {

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GenerateAnEdgeKind.class, "GenerateAnEdgeKind.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(GenerateAnEdgeKind.class, "GenerateAnEdgeKind.description");
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 1000;
    }

    @Override
    public Icon getIcon() {
        return null;
    }


}
