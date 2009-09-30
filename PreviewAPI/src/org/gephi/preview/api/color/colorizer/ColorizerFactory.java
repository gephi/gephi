package org.gephi.preview.api.color.colorizer;

import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;

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
