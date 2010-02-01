/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.predicate;

/**
 *
 * @author Mathieu Bastian
 */
public interface Predicate<T> {

    public boolean evaluate(T element);
}


