/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian, Eduardo Ramos
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.preview.types.editors;

import java.awt.Color;
import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.types.EdgeColor;

/**
 * Basic <code>PropertyEditor</code> for <code>EdgeColor</code>. It is necessary
 * to define this basic editor without CustomEditor support in order to
 * deserialize <code>EdgeColor</code> values from a project file when the full
 * editor (from DesktopPreview module) is not available (when using the toolkit
 * or when the Preview UI is not loaded yet).
 *
 * @author Mathieu Bastian
 */
public class BasicEdgeColorPropertyEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        EdgeColor c = (EdgeColor) getValue();
        if (c.getMode().equals(EdgeColor.Mode.CUSTOM)) {
            Color color = c.getCustomColor() == null ? Color.BLACK : c.getCustomColor();
            return String.format(
                    "%s [%d,%d,%d]",
                    c.getMode().name().toLowerCase(),
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue());
        } else {
            return c.getMode().name().toLowerCase();
        }
    }

    @Override
    public void setAsText(String s) {

        if (matchColorMode(s, EdgeColor.Mode.CUSTOM.name().toLowerCase())) {
            Pattern p = Pattern.compile("\\w+\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
            Matcher m = p.matcher(s);
            if (m.lookingAt()) {
                int r = Integer.valueOf(m.group(1));
                int g = Integer.valueOf(m.group(2));
                int b = Integer.valueOf(m.group(3));

                setValue(new EdgeColor(new Color(r, g, b)));
            }
        } else if (matchColorMode(s, EdgeColor.Mode.MIXED.name().toLowerCase())) {
            setValue(new EdgeColor(EdgeColor.Mode.MIXED));
        } else if (matchColorMode(s, EdgeColor.Mode.ORIGINAL.name().toLowerCase())) {
            setValue(new EdgeColor(EdgeColor.Mode.ORIGINAL));
        } else if (matchColorMode(s, EdgeColor.Mode.SOURCE.name().toLowerCase())) {
            setValue(new EdgeColor(EdgeColor.Mode.SOURCE));
        } else if (matchColorMode(s, EdgeColor.Mode.TARGET.name().toLowerCase())) {
            setValue(new EdgeColor(EdgeColor.Mode.TARGET));
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

    private boolean matchColorMode(String s, String identifier) {
        String regexp = String.format("\\s*%s\\s*", identifier);
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        return m.lookingAt();
    }
}
