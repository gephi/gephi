/*
Copyright 2008-2010 Gephi
Authors : Sebastien Heymann <sebastien.heymann@gephi.org>
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
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;

public class EdgesCustomColor extends PreviewPreset {

    public EdgesCustomColor() {
        super(NbBundle.getMessage(EdgesCustomColor.class, "EdgesCustomColor.name"));

        //Default
        DefaultPreset defaultPreset = new DefaultPreset();
        properties.putAll(defaultPreset.getProperties());
        
        //Custom values
        properties.put(PreviewProperty.NODE_LABEL_SHOW_BOX, Boolean.TRUE);
        properties.put(PreviewProperty.NODE_LABEL_BOX_COLOR, new DependantColor(Color.WHITE));
        properties.put(PreviewProperty.NODE_LABEL_BOX_OPACITY, 80f);
        properties.put(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
        properties.put(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        properties.put(PreviewProperty.NODE_OPACITY, 0);
        properties.put(PreviewProperty.EDGE_COLOR, new EdgeColor(new Color(76, 168, 36)));
    }
}
