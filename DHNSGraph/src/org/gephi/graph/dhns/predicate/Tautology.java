/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.predicate;

/**
 *
 * @author Mathieu Bastian
 */
public class Tautology implements Predicate {

    public static Tautology instance = new Tautology();

    public boolean evaluate(Object element) {
        return true;
    }
}
