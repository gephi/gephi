/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.network.api;

import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Sight;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public interface SightManager {

    public Sight createSight();

    public Sight getMainSight();

    public Sight getSelectedSight();

    public Lookup getModelLookup();

    public void selectSight(Sight sight);

    public void addChangeListener(ChangeListener listener);
}
