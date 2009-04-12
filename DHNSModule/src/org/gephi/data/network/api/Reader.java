/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.network.api;

import java.util.Iterator;

/**
 *
 * @author Mathieu Bastian
 */
public interface Reader {

    public Iterator<? extends NodeWrap> getNodes();

    public Iterator<? extends EdgeWrap> getEdges();

    public boolean requireUpdate();

    public boolean requireNodeUpdate();

    public boolean requireEdgeUpdate();
}
