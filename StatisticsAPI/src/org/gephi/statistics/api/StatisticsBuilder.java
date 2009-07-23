/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.statistics.api;

import org.gephi.statistics.ui.api.StatisticsUI;

/**
 *
 * @author pjmcswee
 */
public interface StatisticsBuilder {


    /**
     *
     * @return
     */
    public Statistics getStatistics();


    /**
     *
     * @return
     */
    public StatisticsUI getUI();

}
