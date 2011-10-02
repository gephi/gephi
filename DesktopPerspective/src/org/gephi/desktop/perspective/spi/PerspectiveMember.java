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
package org.gephi.desktop.perspective.spi;

import org.gephi.desktop.perspective.plugin.LaboratoryPerspective;
import org.gephi.desktop.perspective.plugin.OverviewPerspective;
import org.gephi.desktop.perspective.plugin.PreviewPerspective;

/**
 * Interface to put on <code>TopComponent</code> to say in which perspective it
 * belongs. It has an <b>open</b> and <b>close</b> method to simply say if the
 * component should open or close when asked.
 * <h3>How to set to a TopComponent</h3>
 * <ol><li>Implement this interface to the class that extends <code>TopComponent</code>.</li>
 * <li>Fill <b>open</b> and <b>close</b> methods like explain below.</li>
 * <li>Add the <code>@ServiceProvider</code> annotation to be registered in the system:</li></ol>
 * <pre>
 * <code>@ServiceProvider(service=PerspectiveMember.class)
 * public class MyTopComponent extends TopComponent implements PerpectiveMember {
 * ...
 * </pre>
 * <h3>How to code open and close methods</h3>
 * The code below attach the component to the {@link LaboratoryPerspective}, works also
 * for {@link OverviewPerspective} and {@link PreviewPerspective}:
 * <pre>
 * public boolean open(Perspective perspective) {
 *    returns perspective instanceof LaboratoryPerspective;
 * }
 * public boolean close(Perspective perspective) {
 *    returns true;
 * }
 * </pre>
 * @author Mathieu Bastian
 */
public interface PerspectiveMember {

    /**
     * Returns <code>true</code> if this component opens when
     * <code>perspective</code> opens.
     * @param perspective   the perspective that is to be opened
     * @return              <code>true</code> if this component opens,
     * <code>false</code> otherwise
     */
    public boolean open(Perspective perspective);

    /**
     * Returns <code>true</code> if this component closes when
     * <code>perspective</code> closes.
     * @param perspective   the perspective that is to be closed
     * @return              <code>true</code> if this component closes,
     * <code>false</code> otherwise
     */
    public boolean close(Perspective perspective);
}
