/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

/**
 *
 * @author Martin Škurla
 */
public final class IntegerList extends NumberList<Integer> {

    public IntegerList(byte[] list) {
        this(IntegerList.parse(list));
    }

    public IntegerList(int[] list) {
        super(list, list.length);
    }

    public IntegerList(Integer[] list) {
        super(list);
    }

    public IntegerList(String value) {
        this(value, AbstractList.DEFAULT_SEPARATOR);
    }

    public IntegerList(String value, String separator) {
        super(value, separator, Integer.class);
    }

    private static int[] parse(byte[] list) {
        int[] resultList = new int[list.length];

        for (int i = 0; i < list.length; i++) {
            resultList[i] = list[i];
        }

        return resultList;
    }
}
