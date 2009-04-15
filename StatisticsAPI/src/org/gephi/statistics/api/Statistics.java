/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.statistics.api;

import org.gephi.data.network.api.SyncReader;

/**
 *
 * @author Mathieu Bastian
 */
public interface Statistics {

    public void execute(SyncReader synchReader);
}
