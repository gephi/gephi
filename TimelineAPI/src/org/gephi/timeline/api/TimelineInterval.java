/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.timeline.api;

/**
 *
 * @author jbilcke
 */
public interface TimelineInterval {
    
    public int getLength();

    public String getFirstAttributeLabel();
    public String getLastAttributeLabel();

    public String getAttributeLabel(int i);
    public String getAttributeLabel(int from, int to);

    // may return an average
    public float getAttributeValue(int i);
    public float getAttributeValue(int from, int to);
    
    public TimelineInterval getSubInterval(int from, int to);

    public void addListener(TimelineIntervalListener listener);
    public void removeListener(TimelineIntervalListener listener);
}
