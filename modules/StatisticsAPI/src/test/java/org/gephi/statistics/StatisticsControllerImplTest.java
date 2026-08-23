package org.gephi.statistics;

import java.awt.Dialog;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class StatisticsControllerImplTest {

    @Test
    public void testExecuteFailingStatistics() throws Exception {
        MockServices.setServices(MockStatisticsBuilder.class, MockDialogDisplayer.class);
        MockDialogDisplayer.MESSAGES.clear();

        final StatisticsModelImpl model = new StatisticsModelImpl(new WorkspaceImpl(null, 0));
        StatisticsControllerImpl controller = new StatisticsControllerImpl() {
            @Override
            public StatisticsModelImpl getModel() {
                return model;
            }
        };

        AtomicReference<Throwable> logged = new AtomicReference<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getThrown() instanceof NumberFormatException) {
                    logged.set(record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        CountDownLatch taskFinished = new CountDownLatch(1);
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(handler);
        try {
            controller.execute(new MockStatistics(), task -> taskFinished.countDown());

            String message = MockDialogDisplayer.MESSAGES.poll(10, TimeUnit.SECONDS);
            Assert.assertNotNull("The statistics failure isn't reported to the user", message);
            Assert.assertTrue("The reported failure doesn't name the statistics",
                message.contains(MockStatisticsBuilder.NAME));
            Assert.assertTrue("The listener isn't notified of the failure", taskFinished.await(10, TimeUnit.SECONDS));
        } finally {
            rootLogger.removeHandler(handler);
        }

        Assert.assertNotNull("The statistics failure isn't logged", logged.get());
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

    public static class MockDialogDisplayer extends DialogDisplayer {

        static final BlockingQueue<String> MESSAGES = new LinkedBlockingQueue<>();

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            MESSAGES.add(String.valueOf(descriptor.getMessage()));
            return NotifyDescriptor.CLOSED_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException();
        }
    }
}
