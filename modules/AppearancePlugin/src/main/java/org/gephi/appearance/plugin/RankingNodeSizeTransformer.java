/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.plugin;

import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = RankingTransformer.class)
public class RankingNodeSizeTransformer implements RankingTransformer<Node> {

    protected float minSize = 1f;
    protected float maxSize = 4f;

    @Override
    public void transform(Node node, float rankingValue) {
        float size = rankingValue * (maxSize - minSize) + minSize;
        node.setSize(size);
    }

    public float getMaxSize() {
        return maxSize;
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    public void setMinSize(float minSize) {
        this.minSize = minSize;
    }
}
