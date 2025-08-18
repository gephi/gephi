package org.gephi.layout.plugin.scale;

import org.gephi.layout.spi.LayoutBuilder;

public class ScaleLayout extends AbstractScaleLayout {
    public ScaleLayout(LayoutBuilder layoutBuilder, double scale) {
        super(layoutBuilder, scale);
    }

    @Override
    public void resetPropertiesValues() {
        setScale(1.33);
        setXAxis(true);
        setYAxis(true);
    }

}
