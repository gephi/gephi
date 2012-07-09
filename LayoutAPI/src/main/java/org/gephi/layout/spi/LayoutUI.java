/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.layout.spi;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Various information about a layout algorithm that allows UI integration.
 *
 * @author Mathieu Bastian
 */
public interface LayoutUI {

    /**
     * The description of the layout algorithm purpose.
     * @return  a description snippet for the algorithm
     */
    public String getDescription();

    /**
     * The icon that represents the layout action.
     * @return  a icon for this particular layout
     */
    public Icon getIcon();

    /**
     * A <code>LayoutUI</code> can have a optional settings panel, that will be
     * displayed instead of the property sheet.
     * @param layout the layout that require a simple panel
     * @return A simple settings panel for <code>layout</code> or
     * <code>null</code>
     */
    public JPanel getSimplePanel(Layout layout);

    /**
     * An appraisal of quality for this algorithm. The rank must be between 1 and
     * 5. The rank will be displayed tousers to help them to choose a suitable
     * algorithm. Return -1 if you don't want to display a rank.
     * @return an integer between 1 and 5 or -1 if you don't want to show a rank
     */
    public int getQualityRank();

    /**
     * An appraisal of speed for this algorithm. The rank must be between 1 and
     * 5. The rank will be displayed tousers to help them to choose a suitable
     * algorithm. Return -1 if you don't want to display a rank.
     * @return an integer between 1 and 5 or -1 if you don't want to show a rank
     */
    public int getSpeedRank();
}
