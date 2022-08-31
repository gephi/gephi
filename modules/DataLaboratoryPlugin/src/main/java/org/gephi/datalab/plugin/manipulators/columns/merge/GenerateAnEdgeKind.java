package org.gephi.datalab.plugin.manipulators.columns.merge;

import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Table;
import org.openide.util.NbBundle;

import javax.swing.*;

public class GenerateAnEdgeKind implements AttributeColumnsMergeStrategy {

    private Table table;
    private Column[] columns;

    @Override
    public void setup(Table table, Column[] columns) {
        this.table = table;
        this.columns = columns;
    }

    @Override
    public void execute() {
        Column selectedColumn = this.columns[0];

        for (Edge edge : table.getGraph().getEdges().toArray()) {

            String newType = (String) edge.getAttribute(selectedColumn);
            int edgeType = table.getGraph().getModel().addEdgeType(newType);
            edge.setType(edgeType);

        }
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
        return columns.length == 1 && columns[0].getTypeClass() == String.class;
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
