package org.gephi.desktop.statistics;

import javax.swing.JPanel;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.junit.Assert;
import org.junit.Test;

/**
 * Checks that {@link StatisticsModelUIImpl#getRunning(StatisticsUI)} finds the running
 * {@link Statistics} instance by comparing its class to the {@link StatisticsUI}'s class, the
 * same way {@link StatisticsModelUIImpl#isRunning(StatisticsUI)} does, instead of comparing the
 * class to the instance itself.
 */
public class StatisticsModelUIImplTest {

    @Test
    public void testGetRunningReturnsSameInstanceWhileRunning() {
        StatisticsModelUIImpl model = new StatisticsModelUIImpl(null);
        Statistics statistics = new StubStatistics();
        StatisticsUI ui = new StubStatisticsUI(StubStatistics.class);

        model.setRunning(statistics, true);

        Assert.assertSame(statistics, model.getRunning(ui));
        Assert.assertTrue(model.isRunning(ui));
    }

    @Test
    public void testGetRunningReturnsNullOnceStopped() {
        StatisticsModelUIImpl model = new StatisticsModelUIImpl(null);
        Statistics statistics = new StubStatistics();
        StatisticsUI ui = new StubStatisticsUI(StubStatistics.class);

        model.setRunning(statistics, true);
        model.setRunning(statistics, false);

        Assert.assertNull(model.getRunning(ui));
        Assert.assertFalse(model.isRunning(ui));
    }

    @Test
    public void testGetRunningReturnsNullForAnUnrelatedClass() {
        StatisticsModelUIImpl model = new StatisticsModelUIImpl(null);
        Statistics statistics = new StubStatistics();
        StatisticsUI otherUi = new StubStatisticsUI(OtherStatistics.class);

        model.setRunning(statistics, true);

        Assert.assertNull(model.getRunning(otherUi));
        Assert.assertFalse(model.isRunning(otherUi));
    }

    @Test
    public void testGetRunningReturnsAllRunningStatistics() {
        StatisticsModelUIImpl model = new StatisticsModelUIImpl(null);
        Statistics statistics1 = new StubStatistics();
        Statistics statistics2 = new OtherStatistics();

        model.setRunning(statistics1, true);
        model.setRunning(statistics2, true);

        Assert.assertArrayEquals(new Statistics[] {statistics1, statistics2}, model.getRunning());
    }

    @Test
    public void testGetRunningIsEmptyWhenNothingRunning() {
        StatisticsModelUIImpl model = new StatisticsModelUIImpl(null);

        Assert.assertArrayEquals(new Statistics[0], model.getRunning());
    }

    public static class StubStatistics implements Statistics {

        @Override
        public void execute(GraphModel graphModel) {
        }

        @Override
        public String getReport() {
            return null;
        }
    }

    public static class OtherStatistics implements Statistics {

        @Override
        public void execute(GraphModel graphModel) {
        }

        @Override
        public String getReport() {
            return null;
        }
    }

    public static class StubStatisticsUI implements StatisticsUI {

        private final Class<? extends Statistics> statisticsClass;

        public StubStatisticsUI(Class<? extends Statistics> statisticsClass) {
            this.statisticsClass = statisticsClass;
        }

        @Override
        public JPanel getSettingsPanel() {
            return null;
        }

        @Override
        public void setup(Statistics statistics) {
        }

        @Override
        public void unsetup() {
        }

        @Override
        public Class<? extends Statistics> getStatisticsClass() {
            return statisticsClass;
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "Stub";
        }

        @Override
        public String getShortDescription() {
            return "Stub";
        }

        @Override
        public String getCategory() {
            return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
        }

        @Override
        public int getPosition() {
            return 0;
        }
    }
}
