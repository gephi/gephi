package org.gephi.layout.utils;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.ui.propertyeditor.NodeColumnAllNumbersEditor;
import org.openide.util.Exceptions;

public class MockLayout implements Layout {

    private final MockLayoutBuilder builder;
    private double angle;
    private Column column;
    private double localProperty = 0.0;

    public MockLayout(MockLayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void initAlgo() {

    }

    @Override
    public void setGraphModel(GraphModel graphModel) {

    }

    @Override
    public void goAlgo() {

    }

    @Override
    public boolean canAlgo() {
        return true;
    }

    @Override
    public void endAlgo() {

    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        try {
            properties.add(LayoutProperty.createProperty(
                this, Double.class,
                "angle",
                null,
                "",
                "",
                "getAngle", "setAngle"));
            properties.add(LayoutProperty.createProperty(
                this, Column.class,
                "column",
                null,
                "",
                "getColumn", "setColumn", NodeColumnAllNumbersEditor.class));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        localProperty = 42.0;
    }

    public double getLocalProperty() {
        return localProperty;
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }
}
