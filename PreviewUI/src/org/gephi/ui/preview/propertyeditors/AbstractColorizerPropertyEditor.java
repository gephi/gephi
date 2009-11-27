package org.gephi.ui.preview.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.api.Colorizer;
import org.gephi.preview.api.ColorizerFactory;
import org.openide.util.Lookup;

/**
 *
 * @author jeremy
 */
public abstract class AbstractColorizerPropertyEditor extends PropertyEditorSupport {

    private final ColorizerFactory colorizerFactory = Lookup.getDefault().lookup(ColorizerFactory.class);

    @Override
    public String getAsText() {
        Colorizer c = (Colorizer) getValue();
        return c.toString();
    }

    @Override
    public void setAsText(String s) {

        if (supportsCustomColorMode() && colorizerFactory.matchCustomColorMode(s)) {
            Pattern p = Pattern.compile("\\w+\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
            Matcher m = p.matcher(s);
            if (m.lookingAt()) {
                int r = Integer.valueOf(m.group(1));
                int g = Integer.valueOf(m.group(2));
                int b = Integer.valueOf(m.group(3));

                setValue(colorizerFactory.createCustomColorMode(r, g, b));
            }
        } else if (supportsNodeOriginalColorMode() && colorizerFactory.matchNodeOriginalColorMode(s)) {
            setValue(colorizerFactory.createNodeOriginalColorMode());
        } else if (supportsParentColorMode() && colorizerFactory.matchParentColorMode(s)) {
            setValue(colorizerFactory.createParentColorMode());
        } else if (supportsEdgeB1ColorMode() && colorizerFactory.matchEdgeB1ColorMode(s)) {
            setValue(colorizerFactory.createEdgeB1ColorMode());
        } else if (supportsEdgeB2ColorMode() && colorizerFactory.matchEdgeB2ColorMode(s)) {
            setValue(colorizerFactory.createEdgeB2ColorMode());
        } else if (supportsEdgeBothBColorMode() && colorizerFactory.matchEdgeBothBColorMode(s)) {
            setValue(colorizerFactory.createEdgeBothBColorMode());
        }
    }

    public boolean supportsCustomColorMode() {
        return false;
    }

    public boolean supportsNodeOriginalColorMode() {
        return false;
    }

    public boolean supportsParentColorMode() {
        return false;
    }

    public boolean supportsEdgeB1ColorMode() {
        return false;
    }

    public boolean supportsEdgeB2ColorMode() {
        return false;
    }

    public boolean supportsEdgeBothBColorMode() {
        return false;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    public ColorizerFactory getColorizerFactory() {
        return colorizerFactory;
    }
}
