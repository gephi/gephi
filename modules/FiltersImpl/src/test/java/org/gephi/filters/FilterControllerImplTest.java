package org.gephi.filters;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.filters.api.FilterController;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class FilterControllerImplTest {

    private final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
    private final FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
    private FilterModelImpl filterModel;
    private final AtomicBoolean idleEvent = new AtomicBoolean();

    @Before
    public void setUp() {
        projectController.newProject();
        filterModel = (FilterModelImpl) filterController.getModel();
        filterModel.addChangeListener(e -> {
            if (!filterModel.isRunning()) {
                idleEvent.set(true);
            }
        });
    }

    @After
    public void tearDown() {
        filterController.filterVisible(null);
        projectController.closeCurrentProject();
    }

    @Test
    public void testEventWhenClearedWhileRunning() {
        BlockingFilter filter = new BlockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, filter));
        await().atMost(10, TimeUnit.SECONDS).until(() -> filter.inits.get() == 1);
        Assert.assertTrue(filterModel.isRunning());
        idleEvent.set(false);

        // Clearing resets the visible view under the graph's write lock, which waits for the stopped computation
        // to release its read lock, so the filter is released once this thread blocks there
        Thread testThread = Thread.currentThread();
        Thread releaser = new Thread(() -> {
            try {
                await().atMost(10, TimeUnit.SECONDS).until(() -> testThread.getState() == Thread.State.WAITING);
            } finally {
                filter.permits.release();
            }
        });
        releaser.start();
        filterController.filterVisible(null);

        Assert.assertFalse(filterModel.isRunning());
        Assert.assertTrue("A change event is fired once not running", idleEvent.get());
    }

    @Test
    public void testNoIdleEventWhenReplacedWhileRunning() {
        BlockingFilter first = new BlockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, first));
        await().atMost(10, TimeUnit.SECONDS).until(() -> first.inits.get() == 1);
        idleEvent.set(false);

        BlockingFilter second = new BlockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, second));

        Assert.assertTrue(filterModel.isRunning());
        Assert.assertFalse("Not idle while the new query is computed", idleEvent.get());
        first.permits.release();
        second.permits.release();
        await().atMost(10, TimeUnit.SECONDS).until(() -> !filterModel.isRunning());
        Assert.assertTrue("A change event is fired once the new query is applied", idleEvent.get());
    }
}
