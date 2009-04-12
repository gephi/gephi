/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.data.network.mode;

import org.gephi.data.network.sight.SightImpl;

/**
 *
 * @author Mathieu Bastian
 */
public interface EdgeProcessing {
    public void init(SightImpl sight);
    public void clear(SightImpl sight);
}
