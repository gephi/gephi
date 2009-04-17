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

    public SightManager getSightManager();

    public AsyncReader getAsyncReader(Sight sight);

    public SyncReader getSyncReader(Sight sight);

    public FreeModifier getFreeModifier();

    public FlatImporter getFlatImport();
}
