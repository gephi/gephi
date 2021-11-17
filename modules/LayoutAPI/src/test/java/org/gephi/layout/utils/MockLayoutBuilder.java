package org.gephi.layout.utils;

import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;

public class MockLayoutBuilder implements LayoutBuilder {

    public MockLayoutBuilder() {

    }

    @Override
    public String getName() {
        return "MockLayout";
    }

    @Override
    public LayoutUI getUI() {
        return null;
    }

    @Override
    public MockLayout buildLayout() {
        return new MockLayout(this);
    }
}
