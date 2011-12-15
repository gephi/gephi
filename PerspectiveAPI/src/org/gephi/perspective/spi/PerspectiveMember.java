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
package org.gephi.perspective.spi;

/**
 * Interface to attach a <code>TopComponent</code> to the perspective it belongs.
 * <p>
 * This commands whether a panel should be visible or closed on a particular perspective.
 * <h3>HowTo attach a TopComponent to a perspective</h3>
 * <ol><li>Create a new class which implements the <code>PerspectiveMember</code> interface</li>
 * <li>Implement the <code>isMemberOf()</code> method. Simply test if the given
 * perspective is the one you want to attach the component. For default perspectives, first
 * add a dependency to the <code>DesktopPerspective</code> module and then for instance with preview:
 * <pre>public boolean isMemberOf(Perspective perspective) {
 * return perspective instanceof PreviewPerspective;
 * }</pre></li>
 * <li>Return the unique <code>TopComponent</code> identifier for the <code>getTopComponentId()</code>
 * method. The identifier is defined in the <code>TopComponent</code> annotations.</li>
 * <li>Add <code>@ServiceProvider</code> annotation to your class to be found by
 * the system, like <b>@ServiceProvider(service = PerspectiveMember.class)</b>.</li>
 * </ol>
 * @see Perspective
 * @author Mathieu Bastian
 */
public interface PerspectiveMember {

    /**
     * Returns <code>true</code> if the component belongs to this perspective.
     * @param perspective the perspective to test if the component is member of
     * @return <code>true</code> if this component is member of <code>perspective</code>,
     * <code>false</code> otherwise
     */
    public boolean isMemberOf(Perspective perspective);

    /**
     * Return the <code>TopComponent</code>'s unique identifier. This id is
     * assigned by NetBeans when creating a new component and can be found
     * at the top of the class, in the annotations.
     * @return the <code>TopComponent</code> identifier.
     */
    public String getTopComponentId();
}
