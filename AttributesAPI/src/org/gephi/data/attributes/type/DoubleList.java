/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

/**
 *
 * @author Martin Škurla
 */
public final class DoubleList extends NumberList<Double> {

    public DoubleList(double[] list) {
        super(list, list.length);
    }

    public DoubleList(Double[] list) {
        super(list);
    }

    public DoubleList(String value) {
        this(value, AbstractList.DEFAULT_SEPARATOR);
    }

    public DoubleList(String value, String separator) {
        super(value, separator, Double.class);
    }
}
