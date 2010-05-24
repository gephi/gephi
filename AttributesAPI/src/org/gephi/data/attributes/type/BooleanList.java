/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

/**
 *
 * @author Martin Škurla
 */
public final class BooleanList extends AbstractList<Boolean> {

    /*public BooleanList(boolean[] list) {
        super(list, list.length);
    }*/

    public BooleanList(Boolean[] list) {
        super(list);
    }

    public BooleanList(String value) {
        this(value, AbstractList.DEFAULT_SEPARATOR);
    }

    public BooleanList(String value, String separator) {
        super(value, separator, Boolean.class);
    }
}
