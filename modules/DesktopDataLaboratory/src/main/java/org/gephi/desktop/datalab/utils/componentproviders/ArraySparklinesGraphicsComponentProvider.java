package org.gephi.desktop.datalab.utils.componentproviders;

import java.lang.reflect.Array;
import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.graph.api.AttributeUtils;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author Eduardo Ramos
 */
public class ArraySparklinesGraphicsComponentProvider extends AbstractSparklinesGraphicsComponentProvider {

    public ArraySparklinesGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(graphModelProvider, table);
    }

    @Override
    public String getTextFromValue(Object value) {
        if (value == null) {
            return null;
        }

        return AttributeUtils.printArray(value);
    }

    @Override
    public Number[][] getSparklinesXAndYNumbers(Object arr) {
        int size = Array.getLength(arr);

        Number[] result = new Number[size];
        for (int i = 0; i < size; i++) {
            result[i] = (Number) Array.get(arr, i);//This will do the auto-boxing of primitives
        }

        return new Number[][]{null, result};
    }
}
