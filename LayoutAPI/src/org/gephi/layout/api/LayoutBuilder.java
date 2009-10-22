/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.layout.api;

import javax.swing.Icon;

/**
 * A LayoutBuilder provides a specific Layout instance. The Builder pattern is
 * more suitable for the Layout instantiation to allow simpler reusability of
 * Layout's code.
 *
 * Only the LayoutBuilder of a given layout algorithm is exposed,
 * this way, one can devise different layout algorithms (represented by their
 * respective LayoutBuilder) that uses a same underlying Layout implementation,
 * but that differs only by an aggregation, composition or a property that is
 * set only during instantiation time.
 *
 * See ClockwiseRotate and CounterClockwiseRotate for a simple example of this
 * pattern. Both are LayoutBuilders that instanciate Layouts with a different
 * behaviour (the direction of rotation), but both uses the RotateLayout
 * class. The only difference is the angle provided by the LayoutBuilder on the
 * time of instantiation of the RotateLayout object.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public interface LayoutBuilder {

    /**
     * The name of the behaviour of the Layout's provided by this Builder.
     * @return
     */
    public String getName();

    /**
     * The description of the of the Layout's provided by this Builder.
     * @return
     */
    public String getDescription();

    /**
     * The icon that represents the Layout's provided by this Builder.
     * @return
     */
    public Icon getIcon();

    /**
     * Builds an instance of the Layout.
     * @return
     */
    public Layout buildLayout();
}
