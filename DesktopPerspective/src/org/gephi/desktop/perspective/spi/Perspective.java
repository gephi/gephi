/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.desktop.perspective.spi;

import javax.swing.Icon;

/**
 * Define a group of components that are showed in the banner. <b>Overview</b>, <b>Data
 * Laboratory</b> and <b>Preview</b> are perspectives.
 * <h3>How to add a new Perspective</h3>
 * <ol><li>Create a new module and add <b>Desktop Perspective</b> as dependency.</li>
 * <li>Create a new implementation of perspective and fill methods.</li>
 * <li>Add <code>@ServiceProvider</code> annotation to your class to be found by
 * the system, like <b>@ServiceProvider(service = Perspective.class, position = 500)</b>.</li>
 * <li>Set the position to define the order of appearance, Overview is 100, Preview is 300.</li>
 * </ol>
 * @author Mathieu Bastian
 */
public interface Perspective {

    public String getDisplayName();

    public String getName();

    public Icon getIcon();
}
