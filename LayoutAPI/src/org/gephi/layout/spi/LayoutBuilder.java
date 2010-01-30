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
