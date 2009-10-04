package org.gephi.preview.color.colorizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.api.color.colorizer.Colorizer;
import org.gephi.preview.api.color.colorizer.ColorizerFactory;
import org.gephi.preview.color.colormode.CustomColorMode;
import org.gephi.preview.color.colormode.NodeOriginalColorMode;
import org.gephi.preview.color.colormode.ParentNodeColorMode;

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

	public boolean matchParentNodeColorMode(String s) {
		return matchColorMode(s, ParentNodeColorMode.getIdentifier());
	}

	public boolean isCustomColorMode(Colorizer colorizer) {
		return matchCustomColorMode(colorizer.toString());
	}

	public boolean isNodeOriginalColorMode(Colorizer colorizer) {
		return matchNodeOriginalColorMode(colorizer.toString());
	}

	public boolean isParentNodeColorMode(Colorizer colorizer) {
		return matchParentNodeColorMode(colorizer.toString());
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

	public Colorizer createParentNodeColorMode() {
		return new ParentNodeColorMode();
	}
}
