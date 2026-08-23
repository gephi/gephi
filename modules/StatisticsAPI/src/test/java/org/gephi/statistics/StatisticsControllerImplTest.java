package org.gephi.statistics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.openide.util.Exceptions;

public class StatisticsControllerImplTest {

    @Test
    public void testExecuteFailingStatistics() throws Exception {
        MockServices.setServices(MockStatisticsBuilder.class);

        final StatisticsModelImpl model = new StatisticsModelImpl(new WorkspaceImpl(null, 0));
        StatisticsControllerImpl controller = new StatisticsControllerImpl() {
            @Override
            public StatisticsModelImpl getModel() {
                return model;
            }
        };

        CountDownLatch reported = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getThrown() instanceof NumberFormatException) {
                    thrown.set(record.getThrown());
                    reported.countDown();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(handler);
        try {
            controller.execute(new MockStatistics(), null);
            Assert.assertTrue("The statistics failure isn't reported", reported.await(10, TimeUnit.SECONDS));
        } finally {
            rootLogger.removeHandler(handler);
        }

        String message = Exceptions.findLocalizedMessage(thrown.get());
        Assert.assertNotNull("The reported failure has no message for the user", message);
        Assert.assertTrue("The reported failure doesn't name the statistics",
            message.contains(MockStatisticsBuilder.NAME));
    }

    public static class MockStatistics implements Statistics {

        @Override
        public void execute(GraphModel graphModel) {
            throw new NumberFormatException("Infinite or NaN");
        }

        @Override
        public String getReport() {
            return "";
        }
    }

    public static class MockStatisticsBuilder implements StatisticsBuilder {

        public static final String NAME = "Mock statistics";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Statistics getStatistics() {
            return new MockStatistics();
        }

        @Override
        public Class<? extends Statistics> getStatisticsClass() {
            return MockStatistics.class;
        }
    }
}
