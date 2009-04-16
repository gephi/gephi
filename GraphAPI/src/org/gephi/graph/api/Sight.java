/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.graph.api;

/**
 *
 * @author Mathieu Bastian
 */
public interface Sight {
    public Sight[] getChildren();
    public boolean hasChildren();

    public int getNumber();
    public String getName();
    public void setName(String name);
}
