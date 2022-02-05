package org.gephi.appearance.plugin;

import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.graph.api.Element;

public abstract class RankingSizeTransformer<E extends Element> implements RankingTransformer<E> {

    protected float minSize = 1f;
    protected float maxSize = 4f;

    public float getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMinSize(float minSize) {
        this.minSize = minSize;
    }
}
