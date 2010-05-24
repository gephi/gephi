/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

import java.math.BigInteger;

/**
 *
 * @author Mathieu Bastian
 */
public final class BigIntegerList extends NumberList<BigInteger> {

    public BigIntegerList(BigInteger[] list) {
        super(list);
    }

    public BigIntegerList(String value) {
        this(value, AbstractList.DEFAULT_SEPARATOR);
    }

    public BigIntegerList(String value, String separator) {
        super(value, separator, BigInteger.class);
    }
}

