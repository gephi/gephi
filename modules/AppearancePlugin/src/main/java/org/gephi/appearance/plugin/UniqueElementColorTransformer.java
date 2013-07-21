/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.plugin;

import java.awt.Color;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.graph.api.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = SimpleTransformer.class)
public class UniqueElementColorTransformer implements SimpleTransformer<Element> {

    private Color color;

    @Override
    public void transform(Element element) {
        element.setColor(color);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
