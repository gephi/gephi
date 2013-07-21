/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.plugin;

import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = SimpleTransformer.class)
public class UniqueNodeSizeTransformer implements SimpleTransformer<Node> {

    private float size = 10f;

    @Override
    public void transform(Node node) {
        node.setSize(size);
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
