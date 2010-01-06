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
package org.gephi.layout;

import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class GetPropertySetsTest {

    /* Tests if layouts can generate their propertySets without throwing
     * exceptions (tipically NoSuchMethodException).
     */
    @org.junit.Test
    public void testGetProperties() throws NoSuchMethodException {
        for (LayoutBuilder layoutBuilder : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
            Layout layout = layoutBuilder.buildLayout();
            System.out.println("Layout: " + layoutBuilder.getName());
            layout.getProperties();
        }
    }
}
