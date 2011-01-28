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
package org.gephi.io.importer.api;

import java.util.Collection;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.io.processor.spi.Processor;

/**
 * Interface for unloading a container. Gets graph draft elements and attributes. Get also
 * basic params and properties which defined the content. Unloaders are used by <code>Processor</code>
 * to load data from the container to the main data structure.
 *
 * @author Mathieu Bastian
 * @see Processor
 */
public interface ContainerUnloader {

    public Collection<? extends NodeDraftGetter> getNodes();

    public Collection<? extends EdgeDraftGetter> getEdges();

    public EdgeDraftGetter getEdge(NodeDraftGetter source, NodeDraftGetter target);

    public EdgeDefault getEdgeDefault();

    public AttributeModel getAttributeModel();

    public Double getTimeIntervalMin();

    public Double getTimeIntervalMax();

    public TimeFormat getTimeFormat();

    public boolean allowSelfLoop();

    public boolean allowAutoNode();

    public boolean allowParallelEdges();

    public String getSource();
}
