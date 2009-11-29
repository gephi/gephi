package org.gephi.preview.api;

/**
 * Interface of a node colorizer.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface NodeColorizer extends Colorizer {

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeColorizerClient client);
}
