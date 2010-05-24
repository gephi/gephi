/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

/**
 *
 * @author Martin Škurla
 */
public final class FloatList extends NumberList<Float> {

    public FloatList(float[] list) {
        super(list, list.length);
    }

    public FloatList(Float[] list) {
        super(list);
    }

    public FloatList(String value) {
        this(value, AbstractList.DEFAULT_SEPARATOR);
    }

    public FloatList(String value, String separator) {
        super(value, separator, Float.class);
    }
}
