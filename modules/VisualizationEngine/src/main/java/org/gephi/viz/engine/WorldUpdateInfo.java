package org.gephi.viz.engine;

/**
 * The world update last committed for drawing by {@link VizEngine#display()}.
 *
 * <p>Frames keep being drawn from the previous world data while a world update
 * is pending, so {@link VizEngine#getFrameCount()} advances even when world
 * updaters are stuck, for example waiting for the graph lock. A
 * {@link #sequence()} that stops while frames continue reveals it.</p>
 *
 * <p>Nodes and edges are gathered separately, each under its own graph read
 * lock, so a graph change made during an update may be reflected in one count
 * and not in the other. An update may also be in flight when the graph or the
 * view changes, so counts reflecting a change come from the second update
 * committed after it, not the first.</p>
 *
 * @param sequence  number of world updates committed since the engine was
 *                  created, {@code 0} before the first one
 * @param nodeCount nodes gathered by the update from the visible graph, in or
 *                  near the view boundaries, so all of them when the whole graph
 *                  is in view. An update made while nodes are hidden keeps the
 *                  previous count.
 * @param edgeCount edges gathered by the update, likewise
 */
public record WorldUpdateInfo(long sequence, int nodeCount, int edgeCount) {

    public static final WorldUpdateInfo NONE = new WorldUpdateInfo(0L, 0, 0);
}
