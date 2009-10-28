package org.gephi.preview.color.colorizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.api.color.colorizer.Colorizer;
import org.gephi.preview.api.color.colorizer.ColorizerFactory;
import org.gephi.preview.color.colormode.CustomColorMode;
import org.gephi.preview.color.colormode.EdgeB1ColorMode;
import org.gephi.preview.color.colormode.EdgeB2ColorMode;
import org.gephi.preview.color.colormode.EdgeBothBColorMode;
import org.gephi.preview.color.colormode.NodeOriginalColorMode;
import org.gephi.preview.color.colormode.ParentColorMode;

/**
 *
 * @author jeremy
 */
public class ColorizerFactoryImpl implements ColorizerFactory {

	private boolean matchColorMode(String s, String identifier) {
		String regexp = String.format("\\s*%s\\s*", identifier);
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(s);
		return m.lookingAt();
	}

	public boolean matchCustomColorMode(String s) {
		return matchColorMode(s, CustomColorMode.getIdentifier());
	}

	public boolean matchNodeOriginalColorMode(String s) {
		return matchColorMode(s, NodeOriginalColorMode.getIdentifier());
	}

	public boolean matchParentColorMode(String s) {
		return matchColorMode(s, ParentColorMode.getIdentifier());
	}

	public boolean matchEdgeB1ColorMode(String s) {
		return matchColorMode(s, EdgeB1ColorMode.getIdentifier());
	}

	public boolean matchEdgeB2ColorMode(String s) {
		return matchColorMode(s, EdgeB2ColorMode.getIdentifier());
	}

	public boolean matchEdgeBothBColorMode(String s) {
		return matchColorMode(s, EdgeBothBColorMode.getIdentifier());
	}

	public boolean isCustomColorMode(Colorizer colorizer) {
		return matchCustomColorMode(colorizer.toString());
	}

	public boolean isNodeOriginalColorMode(Colorizer colorizer) {
		return matchNodeOriginalColorMode(colorizer.toString());
	}

	public boolean isParentColorMode(Colorizer colorizer) {
		return matchParentColorMode(colorizer.toString());
	}

	public boolean isEdgeB1ColorMode(Colorizer colorizer) {
		return matchEdgeB1ColorMode(colorizer.toString());
	}

	public boolean isEdgeB2ColorMode(Colorizer colorizer) {
		return matchEdgeB2ColorMode(colorizer.toString());
	}

	public boolean isEdgeBothBColorMode(Colorizer colorizer) {
		return matchEdgeBothBColorMode(colorizer.toString());
	}

	public Colorizer createCustomColorMode(int r, int g, int b) {
		return new CustomColorMode(r, g, b);
	}

	public Colorizer createCustomColorMode(java.awt.Color color) {
		return createCustomColorMode(color.getRed(), color.getGreen(), color.getBlue());
	}

	public Colorizer createNodeOriginalColorMode() {
		return new NodeOriginalColorMode();
	}

	public Colorizer createParentColorMode() {
		return new ParentColorMode();
	}

	public Colorizer createEdgeB1ColorMode() {
		return new EdgeB1ColorMode();
	}

	public Colorizer createEdgeB2ColorMode() {
		return new EdgeB2ColorMode();
	}

	public Colorizer createEdgeBothBColorMode() {
		return new EdgeBothBColorMode();
	}
}
