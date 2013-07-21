/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.plugin;

import java.awt.Color;
import org.gephi.appearance.api.Part;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.graph.api.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = PartitionTransformer.class)
public class PartitionElementColorTransformer implements PartitionTransformer<Element> {

    @Override
    public void transform(Element element, Part part) {
        Color color = part.getColor();
        element.setColor(color);
    }
}
