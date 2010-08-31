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
package org.gephi.tools.spi;

/**
 * Enum setting for {@link Tool} implementations.
 * <ul><li>
 * <b>NONE:</b> Selection features are disabled when the tool is used.</li>
 * <li><b>SELECTION:</b> Selection is enabled when the tool is used.</li>
 * <li><b>SELECTION_AND_DRAGGING:</b> Selection and dragging is enabled when the tool is used.</li>
 * </ul>
 *
 * @author Mathieu Bastian
 */
public enum ToolSelectionType {

    NONE, SELECTION, SELECTION_AND_DRAGGING
}
