package org.gephi.desktop.statistics;

import java.awt.Dialog;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

/**
 * Checks that a statistics algorithm which throws is reported to the user with the name of the
 * failing algorithm and doesn't leave the front-end stuck in the running state.
 */
public class StatisticsControllerUIImplTest {

    private static final String FAILURE_MESSAGE = "Bridging centrality blew up";
    private static final String BUILDER_NAME = "Test Metric";
    private static final String RESULT_VALUE = "42";
    private static final String REPORT = "<html>Report</html>";
    private static final long TIMEOUT_SECONDS = 5;

    private StatisticsControllerUIImpl controllerUI;
    private StatisticsModelUIImpl model;

    @Before
    public void setUp() {
        MockServices.setServices(CapturingDialogDisplayer.class, TestStatisticsBuilder.class, TestStatisticsUI.class);
        CapturingDialogDisplayer.reset();
        TestStatisticsUI.reset();

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = projectController.openNewWorkspace();

        model = new StatisticsModelUIImpl(workspace);
        controllerUI = new StatisticsControllerUIImpl();
        controllerUI.setup(model);
    }

    @After
    public void tearDown() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
        CapturingDialogDisplayer.reset();
        TestStatisticsUI.reset();
        // Services are registered globally, don't leak them into the next test
        MockServices.setServices();
    }

    @Test
    public void testFatalErrorReportsAlgorithmName() throws InterruptedException {
        controllerUI.execute(new TestStatistics(true));

        NotifyDescriptor descriptor = CapturingDialogDisplayer.awaitMessage();
        Assert.assertNotNull("No dialog was shown for the failed execution", descriptor);
        Assert.assertEquals(NotifyDescriptor.ERROR_MESSAGE, descriptor.getMessageType());
        String message = String.valueOf(descriptor.getMessage());
        Assert.assertTrue("Message doesn't name the failing algorithm: " + message, message.contains(BUILDER_NAME));
        Assert.assertTrue("Message doesn't report the error: " + message, message.contains(FAILURE_MESSAGE));

        StatisticsUI ui = statisticsUI();
        Assert.assertFalse("The statistics is still reported as running", model.isRunning(ui));
        Assert.assertNull("A result was added for a failed execution", model.getResult(ui));
        Assert.assertTrue("The statistics UI wasn't unsetup", TestStatisticsUI.isUnsetup());
    }

    @Test
    public void testFatalErrorIsForwardedToListener() throws InterruptedException {
        RecordingListener listener = new RecordingListener();

        controllerUI.execute(new TestStatistics(true), listener);

        Assert.assertTrue("The listener was never notified", listener.await());
        Assert.assertEquals(List.of("fatalError:" + FAILURE_MESSAGE), listener.getCalls());
        Assert.assertNotNull("No dialog was shown for the failed execution",
            CapturingDialogDisplayer.awaitMessage());
        Assert.assertFalse("The statistics is still reported as running", model.isRunning(statisticsUI()));
    }

    @Test
    public void testSuccessIsUnaffected() throws InterruptedException {
        RecordingListener listener = new RecordingListener();

        controllerUI.execute(new TestStatistics(false), listener);

        Assert.assertTrue("The listener was never notified", listener.await());
        Assert.assertEquals(List.of("taskFinished"), listener.getCalls());
        Assert.assertNull("A dialog was shown for a successful execution", CapturingDialogDisplayer.pollMessage());

        StatisticsUI ui = statisticsUI();
        Assert.assertFalse("The statistics is still reported as running", model.isRunning(ui));
        Assert.assertEquals(RESULT_VALUE, model.getResult(ui));
        Assert.assertEquals(REPORT, model.getReport(TestStatistics.class));
        Assert.assertTrue("The statistics UI wasn't unsetup", TestStatisticsUI.isUnsetup());
    }

    private StatisticsUI statisticsUI() {
        StatisticsUI[] uis = controllerUI.getUI(new TestStatistics(false));
        Assert.assertEquals("The test StatisticsUI isn't registered in Lookup", 1, uis.length);
        return uis[0];
    }

    private static final class RecordingListener implements LongTaskListener {

        private final List<String> calls = new CopyOnWriteArrayList<>();
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void taskFinished(LongTask task) {
            calls.add("taskFinished");
            latch.countDown();
        }

        @Override
        public void fatalError(Throwable t) {
            calls.add("fatalError:" + t.getMessage());
            latch.countDown();
        }

        boolean await() throws InterruptedException {
            return latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        List<String> getCalls() {
            return List.copyOf(calls);
        }
    }

    public static class TestStatistics implements Statistics {

        private final boolean fail;

        public TestStatistics(boolean fail) {
            this.fail = fail;
        }

        @Override
        public void execute(GraphModel graphModel) {
            if (fail) {
                throw new IllegalStateException(FAILURE_MESSAGE);
            }
        }

        @Override
        public String getReport() {
            return REPORT;
        }
    }

    public static class TestStatisticsBuilder implements StatisticsBuilder {

        @Override
        public String getName() {
            return BUILDER_NAME;
        }

        @Override
        public Statistics getStatistics() {
            return new TestStatistics(true);
        }

        @Override
        public Class<? extends Statistics> getStatisticsClass() {
            return TestStatistics.class;
        }
    }

    public static class TestStatisticsUI implements StatisticsUI {

        private static volatile boolean unsetup;

        static void reset() {
            unsetup = false;
        }

        static boolean isUnsetup() {
            return unsetup;
        }

        @Override
        public JPanel getSettingsPanel() {
            return null;
        }

        @Override
        public void setup(Statistics statistics) {
            unsetup = false;
        }

        @Override
        public void unsetup() {
            unsetup = true;
        }

        @Override
        public Class<? extends Statistics> getStatisticsClass() {
            return TestStatistics.class;
        }

        @Override
        public String getValue() {
            return RESULT_VALUE;
        }

        @Override
        public String getDisplayName() {
            return BUILDER_NAME;
        }

        @Override
        public String getShortDescription() {
            return BUILDER_NAME;
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

    public static class CapturingDialogDisplayer extends DialogDisplayer {

        private static final LinkedBlockingQueue<NotifyDescriptor> MESSAGES = new LinkedBlockingQueue<>();

        static void reset() {
            MESSAGES.clear();
        }

        static NotifyDescriptor awaitMessage() throws InterruptedException {
            return MESSAGES.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        static NotifyDescriptor pollMessage() {
            return MESSAGES.poll();
        }

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            MESSAGES.add(descriptor);
            return NotifyDescriptor.OK_OPTION;
        }

        @Override
        public void notifyLater(NotifyDescriptor descriptor) {
            //Capture synchronously, the default implementation defers to the event thread
            MESSAGES.add(descriptor);
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException();
        }
    }
}
