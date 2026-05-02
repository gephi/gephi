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

package org.gephi.desktop.preview.propertyeditors;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.netbeans.beaninfo.editors.FontEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

/**
 * A custom FontEditor that renders disabled (non-writable) properties with reduced opacity.
 * <p>
 * The default NetBeans FontEditor doesn't grey out the text when the property is disabled.
 * This editor extends it and implements ExPropertyEditor to access the PropertyEnv,
 * which allows checking if the property is writable.
 *
 * @author Mathieu Bastian
 */
public class DisabledAwareFontEditor extends FontEditor implements ExPropertyEditor {

    private PropertyEnv env;

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
        boolean disabled = false;

        if (env != null && env.getFeatureDescriptor() instanceof Node.Property) {
            Node.Property<?> prop = (Node.Property<?>) env.getFeatureDescriptor();
            disabled = !prop.canWrite();
        }

        if (disabled) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            super.paintValue(g2, rectangle);
            g2.dispose();
        } else {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            super.paintValue(g, rectangle);
        }
    }
}

