/*
Copyright 2008-2011 Gephi
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
package org.gephi.datalab.spi.rows.merge;

/**
 * <p>This interface is used for providing RowsMergeStrategy instances
 * using the Netbeans Lookup but avoiding the singleton it causes.</p>
 * <p>Each RowsMergeStrategy should have a RowsMergeStrategyBuilder and publish it with <code>@ServiceProvider(service=RowsMergeStrategyBuilder.class)</code> annotation.</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeRowsMergeStrategyBuilder {

    AttributeRowsMergeStrategy getAttributeRowsMergeStrategy();
}
