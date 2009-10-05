/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.ranking;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeColumn;

/**
 *
 * @author Mathieu Bastian
 */
public class TransformerFactory {

    public ColorTransformer getColorTransformer(AttributeColumn attributeColumn) {
        AbstractColorTransformer colorTransformer=null;
        switch(attributeColumn.getAttributeType()) {
            case DOUBLE:
                colorTransformer = getDoubleColorTransformer();
                break;
            case FLOAT:
                colorTransformer = getFloatColorTransformer();
                break;
            case INT:
                colorTransformer = getIntegerColorTransformer();
                break;
            case LONG:
                colorTransformer = getLongColorTransformer();
                break;
        }
        return colorTransformer;
    }

    private AbstractColorTransformer<Double> getDoubleColorTransformer() {
        return new AbstractColorTransformer<Double>() {

            public Color transform(Double value) {
                float ratio = (float)(value-lowerBound/(upperBound-lowerBound));
                return linearGradient.getValue(ratio);
            }
        };
    }

    private AbstractColorTransformer<Float> getFloatColorTransformer() {
        return new AbstractColorTransformer<Float>() {

            public Color transform(Float value) {
                float ratio = value-lowerBound/(upperBound-lowerBound);
                return linearGradient.getValue(ratio);
            }
        };
    }

    private AbstractColorTransformer<Integer> getIntegerColorTransformer() {
        return new AbstractColorTransformer<Integer>() {

            public Color transform(Integer value) {
                float ratio = (float)(value-lowerBound)/(upperBound-lowerBound);
                return linearGradient.getValue(ratio);
            }
        };
    }

    private AbstractColorTransformer<Long> getLongColorTransformer() {
        return new AbstractColorTransformer<Long>() {

            public Color transform(Long value) {
                float ratio = (float)((double)(value-lowerBound)/(upperBound-lowerBound));
                return linearGradient.getValue(ratio);
            }
        };
    }
}
