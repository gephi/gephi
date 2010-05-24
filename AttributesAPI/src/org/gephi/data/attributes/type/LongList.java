/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

/**
 *
 * @author Martin Škurla
 */
public final class LongList extends NumberList<Long> {

    public LongList(long[] list) {
        super(list, list.length);
    }

    public LongList(Long[] list) {
        super(list);
    }

    public LongList(String value) {
        this(value, AbstractList.DEFAULT_SEPARATOR);
    }

    public LongList(String value, String separator) {
        super(value, separator, Long.class);
    }
}
