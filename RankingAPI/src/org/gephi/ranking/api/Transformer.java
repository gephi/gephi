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
package org.gephi.ranking.api;

/**
 * Transformers role is to transform nodes/edges numerical attributes 
 * to visual signs (e.g. color or sizes). It uses a normalized real number
 * between zero and one to output a meaningful value.
 * <p>
 * Transformers can be applied to a subset of values using lower/bound filter
 * values.
 * <p>
 * Default transformers implemented in the RankingPlugin:
 * <ul><li><b>RENDERABLE_COLOR:</b> Sets node/edge color</li>
 * <li><b>RENDERABLE_SIZE:</b> Sets node/edge size. For edges, the size is the weight.</li>
 * <li><b>LABEL_COLOR:</b> Sets label color.</li>
 * <li><b>LABEL_SIZE:</b> Sets label size. Note this is a multiplier.</li>
 * 
 * @see Ranking
 * @author Mathieu Bastian
 */
public interface Transformer<Target> {

    public static final String RENDERABLE_COLOR = "renderable_color";
    public static final String RENDERABLE_SIZE = "renderable_size";
    public static final String LABEL_COLOR = "label_color";
    public static final String LABEL_SIZE = "label_size";

    /**
     * Sets the lower filter bound. Values lower than this value won't be
     * transformed. By default the bound is set to zero, so no filtering.
     * @param lowerBound the lower bound filter value
     */
    public void setLowerBound(float lowerBound);

    /**
     * Sets the upper filter bound. Values upper than this value won't be
     * transformed. By default the bound is set to one, so no filtering.
     * @param upperBound the upper bound filter value
     */
    public void setUpperBound(float upperBound);

    /**
     * Returns the lower bound filter value. By default it's set to zero, so
     * filtering is disabled.
     * @return the lower bound filter value
     */
    public float getLowerBound();

    /**
     * Returns the upper bound filter value. By default it's set to one, so
     * filtering is disabled.
     * @return the upper bound filter value
     */
    public float getUpperBound();

    /**
     * Returns <code>true</code> if <code>value</code> is within the lower and
     * the upper bound. Typically, this is called before <code>transform()</code>
     * to know if a value can be processed. By default, this always returns <code>
     * true</code>, as lower bound is set ot zero and upper bound to one.
     * @param value the value to test if in bounds
     * @return <code>true</code> if value superior or equal to lowerBound and
     * value inferior or equal to upperBound, <code>false</code> otherwise
     * 
     */
    public boolean isInBounds(float value);

    /**
     * Transforms <code>target</code> with <code>normalizedValue</code> between
     * zero and one. The method also returns the transformed value, like the color
     * for instance for a color transformer.
     * @param target            the object to transform
     * @param normalizedValue   the ranking normalized value
     * @return                  the transformed value, or <code>null</code>
     */
    public Object transform(Target target, float normalizedValue);
}
