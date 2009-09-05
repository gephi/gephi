package org.gephi.preview.api.color;

/**
 *
 * @author jeremy
 */
public interface ColorizerFactory {

    public GenericColorizer createGenericColorizer(ColorizerType type);

    public NodeColorizer createNodeColorizer(ColorizerType type);

    public NodeChildColorizer createNodeChildColorizer(ColorizerType type);

    public EdgeColorizer createEdgeColorizer(ColorizerType type);

    public EdgeChildColorizer createEdgeChildColorizer(ColorizerType type);
}
