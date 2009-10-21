package org.gephi.preview.api.color.colorizer;

/**
 *
 * @author jeremy
 */
public interface ColorizerFactory {

    public boolean matchCustomColorMode(String s);

    public boolean matchNodeOriginalColorMode(String s);

    public boolean matchParentNodeColorMode(String s);

	public boolean matchEdgeB1ColorMode(String s);

	public boolean matchEdgeB2ColorMode(String s);

	public boolean matchEdgeBothBColorMode(String s);

	public boolean isCustomColorMode(Colorizer colorizer);

	public boolean isNodeOriginalColorMode(Colorizer colorizer);

	public boolean isParentNodeColorMode(Colorizer colorizer);

	public boolean isEdgeB1ColorMode(Colorizer colorizer);

	public boolean isEdgeB2ColorMode(Colorizer colorizer);

	public boolean isEdgeBothBColorMode(Colorizer colorizer);

    public Colorizer createCustomColorMode(int r, int g, int b);

	public Colorizer createCustomColorMode(java.awt.Color color);

    public Colorizer createNodeOriginalColorMode();

    public Colorizer createParentNodeColorMode();

	public Colorizer createEdgeB1ColorMode();

	public Colorizer createEdgeB2ColorMode();

	public Colorizer createEdgeBothBColorMode();
}
