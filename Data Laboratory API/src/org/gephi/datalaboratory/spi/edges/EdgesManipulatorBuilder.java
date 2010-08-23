/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.spi.edges;

/**
 * <p>This interface is used for providing EdgesManipulator instances
 * using the Netbeans Lookup but avoiding the singleton it causes.</p>
 * <p>Each EdgesManipulator should have a EdgesManipulatorBuilder
 * with <code>@ServiceProvider(service=EdgesManipulatorBuilder.class)</code> annotation to be public.</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface EdgesManipulatorBuilder {

    EdgesManipulator getEdgesManipulator();
}
