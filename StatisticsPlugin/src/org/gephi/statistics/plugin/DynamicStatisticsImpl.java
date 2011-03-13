/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.statistics.plugin;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 * The default implementation of {@link DynamicStatistics}.
 *
 * @author Cezary Bartosiak
 */
public abstract class DynamicStatisticsImpl implements DynamicStatistics, LongTask {

    protected TimeInterval timeInterval = new TimeInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    protected double window = Double.POSITIVE_INFINITY - Double.NEGATIVE_INFINITY;
    protected Estimator estimator = Estimator.FIRST;
    private boolean cancel;
    private ProgressTicket progressTicket;
    private String graphRevision;

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public double getWindow() {
        return window;
    }

    public Estimator getEstimator() {
        return estimator;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public void setWindow(double window) {
        if (window < 0 || window > timeInterval.getHigh() - timeInterval.getLow()) {
            throw new IllegalArgumentException(
                    "The window must be greater than 0 "
                    + "and less or equal to (high - low).");
        }

        this.window = window;
    }

    public void setEstimator(Estimator estimator) {
        if (estimator != Estimator.MEDIAN && estimator != Estimator.MODE
                && estimator != Estimator.FIRST && estimator != Estimator.LAST) {
            throw new IllegalArgumentException(
                    "The given estimator must be one of the following: "
                    + "MEDIAN, MODE, FIRST, LAST.");
        }

        this.estimator = estimator;
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel dynamicModel = dynamicController.getModel();
        DynamicGraph dynamicGraph = dynamicModel.createDynamicGraph(graph, timeInterval);

        cancel = false;
        graph.writeLock();

        graphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";

        // fire abstract preloop method
        preloop();

        int progress = 0;
        Progress.start(progressTicket, progress);

        for (double low = timeInterval.getLow(); low <= timeInterval.getHigh() - window;
                low += (window < 1.0 ? 1.0 : window)) {
            double high = low + window;

            Graph g = dynamicGraph.getSnapshotGraph(low, high, estimator);

            // fire abstract inloop method
            inloop(low, high, (HierarchicalGraph) g, attributeModel);

            Progress.progress(progressTicket, ++progress);
            if (cancel) {
                graph.writeUnlock();
                return;
            }
        }

        graph.writeUnlock();
    }

    /**
     * Fires before the loop on all time intervals.
     */
    protected abstract void preloop();

    /**
     * Fires during the loop on all time intervals.
     *
     * @param low            the left endpoint of the current interval
     * @param high           the right endpoint of the current interval
     * @param g              the snapshot graph
     * @param attributeModel the attributes model to write results to
     */
    protected abstract void inloop(double low, double high, HierarchicalGraph g, AttributeModel attributeModel);

    public String getReport() {
        String start = "-inf";
        String end = "+inf";
        if (!Double.isInfinite(timeInterval.getLow())) {
            start = DynamicUtilities.getXMLDateStringFromDouble(timeInterval.getLow()).replace('T', ' ').
                    substring(0, 19);
        }
        if (!Double.isInfinite(timeInterval.getHigh())) {
            end = DynamicUtilities.getXMLDateStringFromDouble(timeInterval.getHigh()).replace('T', ' ').
                    substring(0, 19);
        }

        String windowString = (int) Math.round(window / (timeInterval.getHigh() - timeInterval.getLow()) * 100) + "";

        String report = new String(
                "<html><body><h1>" + getReportName() + "</h1>"
                + "<hr><br><h2>Network Revision Number:</h2>"
                + graphRevision
                + "<br>"
                + "<h2>Parameters:</h2>"
                + "Time interval: " + "[" + start + ", " + end + "]<br>"
                + "Window: " + windowString + " %<br>"
                + "Estimator: " + estimator + "<br>"
                + getAdditionalParameters()
                + "<h2>Results:</h2>"
                + getResults()
                + "</body></html>");

        return report;
    }

    /**
     * Returns the name of the report.
     *
     * @return the name of the report.
     */
    protected abstract String getReportName();

    /**
     * Returns html with additional parameters.
     *
     * @return html with additional parameters.
     */
    protected abstract String getAdditionalParameters();

    /**
     * Returns html with results of firing this dynamic metric.
     *
     * @return html with results of firing this dynamic metric.
     */
    protected abstract String getResults();

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
