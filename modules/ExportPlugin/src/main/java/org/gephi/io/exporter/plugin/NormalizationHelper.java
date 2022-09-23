package org.gephi.io.exporter.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class NormalizationHelper {

    public static NormalizationHelper build(boolean enabled, Graph graph) {
        return new NormalizationHelper(enabled, graph);
    }

    private final boolean enabled;
    protected final float minSize;
    protected final float maxSize;
    protected final float minX;
    protected final float maxX;
    protected final float minY;
    protected final float maxY;
    protected final float minZ;
    protected final float maxZ;

    private NormalizationHelper(boolean enabled, Graph graph) {
        this.enabled = enabled;

        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        float minSize = Float.POSITIVE_INFINITY;
        float maxSize = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getNodes()) {
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
            minZ = Math.min(minZ, node.z());
            maxZ = Math.max(maxZ, node.z());
            minSize = Math.min(minSize, node.size());
            maxSize = Math.max(maxSize, node.size());
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected float normalizeX(float x) {
        if (enabled && x != 0.0) {
            return (x - minX) / (maxX - minX);
        } else {
            return x;
        }
    }

    protected float normalizeY(float y) {
        if (enabled && y != 0.0) {
            return (y - minY) / (maxY - minY);
        } else {
            return y;
        }
    }

    protected float normalizeZ(float z) {
        if (enabled && z != 0.0) {
            return (z - minZ) / (maxZ - minZ);
        } else {
            return z;
        }
    }

    protected float normalizeSize(float size) {
        if (enabled && size != 0.0) {
            return (size - minSize) / (maxSize - minSize);
        } else {
            return size;
        }
    }
}
