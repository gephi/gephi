/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.network.api;

import org.gephi.graph.api.Sight;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public interface SightManager {

    public Sight createSight();

    public Sight getMainSight();

    public Lookup getModelLookup();

    public void selectSight(Sight sight);
}
