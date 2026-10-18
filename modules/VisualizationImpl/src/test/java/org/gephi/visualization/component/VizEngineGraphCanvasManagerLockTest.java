package org.gephi.visualization.component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.gephi.visualization.VizController;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * {@link VizEngineGraphCanvasManager#init} holds the manager monitor while it waits for the EDT, both explicitly and
 * inside JOGL's native window initialisation. What the EDT calls on the manager must therefore never need that
 * monitor, otherwise the application hangs at startup when the Graph window is activated while the engine initialises.
 */
public class VizEngineGraphCanvasManagerLockTest {

    @Test(timeout = 60000)
    public void testRequestCanvasFocusDoesNotBlockTheEdtWhileInitWaitsForIt() throws Exception {
        final VizEngineGraphCanvasManager manager =
            new VizEngineGraphCanvasManager(Mockito.mock(VizController.class));
        final CountDownLatch monitorHeld = new CountDownLatch(1);
        final CountDownLatch focusRequestQueued = new CountDownLatch(1);
        final CountDownLatch initDone = new CountDownLatch(1);

        // Like init(): holds the monitor on a worker thread, then waits for the EDT
        final Thread init = new Thread(() -> {
            synchronized (manager) {
                monitorHeld.countDown();
                try {
                    focusRequestQueued.await();
                    SwingUtilities.invokeAndWait(() -> {
                    });
                    initDone.countDown();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "test-init");
        init.start();
        try {
            Assert.assertTrue(monitorHeld.await(10, TimeUnit.SECONDS));

            // Like TopComponentFocus when the Graph window is activated: the EDT is asked for the focus before it
            // can serve the request that init() waits for
            SwingUtilities.invokeLater(manager::requestCanvasFocus);
            focusRequestQueued.countDown();

            Assert.assertTrue("The EDT is blocked on the manager monitor held by init()",
                initDone.await(15, TimeUnit.SECONDS));
        } finally {
            // Frees the monitor if the EDT is stuck, so that a failure does not hang the test JVM
            init.interrupt();
            init.join(10000);
        }
    }
}
