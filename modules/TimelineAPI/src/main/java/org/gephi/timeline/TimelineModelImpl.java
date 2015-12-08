/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.timeline;

import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.timeline.api.TimelineChart;
import org.gephi.timeline.api.TimelineModel;

/**
 *
 * @author Mathieu Bastian
 */
public class TimelineModelImpl implements TimelineModel {

    private boolean enabled;
    private final GraphModel graphModel;
    private double customMin;
    private double customMax;
    //Animation
    private int playDelay;
    private final AtomicBoolean playing;
    private double playStep;
    private PlayMode playMode;
    //Chart
    private TimelineChart chart;
    //MinMax
    private double previousMin;
    private double previousMax;
    //
    private Interval interval;

    public TimelineModelImpl(GraphModel dynamicModel) {
        this.graphModel = dynamicModel;
        this.customMin = dynamicModel.getTimeBounds().getLow();
        this.customMax = dynamicModel.getTimeBounds().getHigh();
        this.previousMin = customMin;
        this.previousMax = customMax;
        playDelay = 100;
        playStep = 0.01;
        playing = new AtomicBoolean(false);
        playMode = PlayMode.TWO_BOUNDS;
        interval = new Interval(customMin, customMax);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public double getMin() {
        return graphModel.getTimeBounds().getLow();
    }

    @Override
    public double getMax() {
        return graphModel.getTimeBounds().getHigh();
    }

    public double getPreviousMin() {
        return previousMin;
    }

    public double getPreviousMax() {
        return previousMax;
    }

    public void setPreviousMax(double previousMax) {
        this.previousMax = previousMax;
    }

    public void setPreviousMin(double previousMin) {
        this.previousMin = previousMin;
    }

    @Override
    public double getCustomMin() {
        return customMin;
    }

    @Override
    public double getCustomMax() {
        return customMax;
    }

    @Override
    public boolean hasCustomBounds() {
        Interval tm = graphModel.getTimeBounds();
        return customMax != tm.getHigh() || customMin != tm.getLow();
    }

    @Override
    public double getIntervalStart() {
        double vi = interval.getLow();
        if (Double.isInfinite(vi)) {
            return getCustomMin();
        }
        return vi;
    }

    @Override
    public double getIntervalEnd() {
        double vi = interval.getHigh();
        if (Double.isInfinite(vi)) {
            return getCustomMax();
        }
        return vi;
    }

    @Override
    public TimeFormat getTimeFormat() {
        return graphModel.getTimeFormat();
    }

    public void setCustomMax(double customMax) {
        this.customMax = customMax;
    }

    public void setCustomMin(double customMin) {
        this.customMin = customMin;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setInterval(double start, double end) {
        this.interval = new Interval(start, end);
    }

    @Override
    public boolean hasValidBounds() {
        Interval i = graphModel.getTimeBounds();
        return !Double.isInfinite(i.getLow()) && !Double.isInfinite(i.getHigh());
    }

    public GraphModel getGraphModel() {
        return graphModel;
    }

    @Override
    public boolean isPlaying() {
        return playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public int getPlayDelay() {
        return playDelay;
    }

    public void setPlayDelay(int playDelay) {
        this.playDelay = playDelay;
    }

    @Override
    public double getPlayStep() {
        return playStep;
    }

    public void setPlayStep(double playStep) {
        this.playStep = playStep;
    }

    @Override
    public PlayMode getPlayMode() {
        return playMode;
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    @Override
    public TimelineChart getChart() {
        return chart;
    }

    public void setChart(TimelineChart chart) {
        this.chart = chart;
    }
}
