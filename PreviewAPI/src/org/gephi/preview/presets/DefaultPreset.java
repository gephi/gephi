/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.presets;

import java.awt.Color;
import java.awt.Font;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class DefaultPreset extends PreviewPreset {

    public DefaultPreset() {
        super(NbBundle.getMessage(DefaultPreset.class, "Default.name"));

        properties.put(PreviewProperty.ARROW_SIZE, 3f);
        properties.put(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);

        properties.put(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.MIXED));
        properties.put(PreviewProperty.EDGE_CURVED, true);
        properties.put(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.FALSE);
        properties.put(PreviewProperty.EDGE_OPACITY, 100f);
        properties.put(PreviewProperty.EDGE_RADIUS, 0f);
        properties.put(PreviewProperty.EDGE_THICKNESS, 1f);

        properties.put(PreviewProperty.EDGE_LABEL_COLOR, new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
        properties.put(PreviewProperty.EDGE_LABEL_FONT, new Font("Arial", Font.PLAIN, 10));
        properties.put(PreviewProperty.EDGE_LABEL_MAX_CHAR, 30);
        properties.put(PreviewProperty.EDGE_LABEL_OUTLINE_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.EDGE_LABEL_OUTLINE_OPACITY, 80f);
        properties.put(PreviewProperty.EDGE_LABEL_OUTLINE_SIZE, 0);
        properties.put(PreviewProperty.EDGE_LABEL_SHORTEN, false);

        properties.put(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(Color.BLACK));
        properties.put(PreviewProperty.NODE_BORDER_WIDTH, 1.0f);
        properties.put(PreviewProperty.NODE_OPACITY, 100f);

        properties.put(PreviewProperty.NODE_LABEL_BOX_COLOR, new DependantColor(DependantColor.Mode.PARENT));
        properties.put(PreviewProperty.NODE_LABEL_BOX_OPACITY, 100f);
        properties.put(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
        properties.put(PreviewProperty.NODE_LABEL_FONT, new Font("Arial", Font.PLAIN, 12));
        properties.put(PreviewProperty.NODE_LABEL_MAX_CHAR, 30);
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_OPACITY, 80f);
        properties.put(PreviewProperty.NODE_LABEL_OUTLINE_SIZE, 0);
        properties.put(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, true);
        properties.put(PreviewProperty.NODE_LABEL_SHORTEN, false);
        properties.put(PreviewProperty.NODE_LABEL_SHOW_BOX, false);

        properties.put(PreviewProperty.SHOW_EDGES, Boolean.TRUE);
        properties.put(PreviewProperty.SHOW_EDGE_LABELS, Boolean.FALSE);
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
    }
}
