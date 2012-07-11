/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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

/**
 * A <code>LayoutBuilder</code> provides a specific {@link Layout} instance. The
 * Builder pattern is more suitable for the Layout instantiation to allow
 * simpler reusability of Layout's code.
 *<p>
 * Only the LayoutBuilder of a given layout algorithm is exposed,
 * this way, one can devise different layout algorithms (represented by their
 * respective LayoutBuilder) that uses a same underlying Layout implementation,
 * but that differs only by an aggregation, composition or a property that is
 * set only during instantiation time.
 *<p>
 * See <code>ClockwiseRotate</code> and <code>CounterClockwiseRotate</code> for
 * a simple example of this pattern. Both are LayoutBuilders that instanciate
 * Layouts with a different behaviour (the direction of rotation), but both uses
 * the RotateLayout class. The only difference is the angle provided by the
 * LayoutBuilder on the time of instantiation of the RotateLayout object.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public interface LayoutBuilder {

    /**
     * The name of the behaviour of the Layout's provided by this Builder.
     * @return  the display neame of the layout algorithm
     */
    public String getName();

    /**
     * User interface attributes (name, description, icon...) for all Layouts
     * built by this builder.
     * @return a <code>LayoutUI</code> instance
     */
    public LayoutUI getUI();

    /**
     * Builds an instance of the Layout.
     * @return  a new <code>Layout</code> instance
     */
    public Layout buildLayout();
}
