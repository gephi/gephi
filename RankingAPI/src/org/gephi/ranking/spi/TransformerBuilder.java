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
package org.gephi.ranking.spi;

import org.gephi.ranking.api.Transformer;

/**
 * Transformer builder, creating <code>Transformer</code> instances.
 * <p>
 * Implementors should add the <code>@ServiceProvider</code> annotation to be
 * registered by the system.
 * <p>
 * @see Transformer
 * @author Mathieu Bastian
 */
public interface TransformerBuilder {

    /**
     * Build a new <code>transformer</code> instance.
     * @return a new transformer
     */
    public Transformer buildTransformer();

    /**
     * Returns <code>true</code> if this builder is creating transformers 
     * working with <code>elementType</code>. Element types can be
     * <code>Ranking.NODE_ELEMENT</code> or <code>Ranking.EDGE_ELEMENT</code> and
     * defines the type of element rankings and transformers can manipulate.
     * @param elementType the type of element
     * @return <code>true</code> if the transformer can be used on <code>elementType</code>,
     * <code>false</code> otherwise.
     */
    public boolean isTransformerForElement(String elementType);

    /**
     * Returns the name of the transformer built by this builder. Default names
     * are defined in the {@link Transformer} interface.
     * @return the name of the transformer
     */
    public String getName();
}
