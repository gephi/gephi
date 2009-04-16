/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.network.api;

import org.gephi.graph.api.Sight;

/**
 *
 * @author Mathieu Bastian
 */
public interface DhnsController {

    public Sight getMainSight();

    public AsyncReader getAsyncReader();

    public SyncReader getSyncReader();

    public FreeModifier getFreeModifier();
}
