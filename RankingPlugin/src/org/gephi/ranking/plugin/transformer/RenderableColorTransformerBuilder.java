/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.ranking.plugin.transformer;

import java.awt.Color;
import org.gephi.graph.api.Renderable;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.spi.TransformerBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Renderable color transformer builder. Builds <code>RenderableColorTransformer</code>
 * instances, that receives {@link Renderable} targets. Renderable can be nodes or
 * edges data.
 * 
 * @author Mathieu Bastian
 */
@ServiceProvider(service = TransformerBuilder.class, position = 100)
public class RenderableColorTransformerBuilder implements TransformerBuilder {

    @Override
    public Transformer buildTransformer() {
        return new RenderableColorTransformer();
    }

    @Override
    public boolean isTransformerForElement(String elementType) {
        return elementType.equals(Ranking.NODE_ELEMENT) || elementType.equals(Ranking.EDGE_ELEMENT);
    }

    @Override
    public String getName() {
        return Transformer.RENDERABLE_COLOR;
    }

    public static class RenderableColorTransformer extends AbstractColorTransformer<Renderable> {

        @Override
        public Object transform(Renderable target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            return color;
        }
    }
}
