package org.gephi.filters;

import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
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
    private final List<BlockingFilter> filters = new ArrayList<>();
    private final AtomicBoolean idleEvent = new AtomicBoolean();
    private FilterModelImpl filterModel;

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
        // A blocked filter holds the graph's write lock, which clearing the filter below waits for, so a failed
        // assertion would otherwise hang the build instead of failing it
        filters.forEach(filter -> filter.permits.release(1_000));
        filterController.filterVisible(null);
        projectController.closeCurrentProject();
    }

    @Test
    public void testEventWhenClearedWhileRunning() {
        BlockingFilter filter = blockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, filter));
        await().atMost(10, TimeUnit.SECONDS).until(() -> filter.inits.get() == 1);
        Assert.assertTrue(filterModel.isRunning());
        idleEvent.set(false);

        // Clearing resets the visible view under the graph's write lock, which the stopped computation holds, so
        // the filter is released once this thread blocks there
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
        BlockingFilter first = blockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, first));
        await().atMost(10, TimeUnit.SECONDS).until(() -> first.inits.get() == 1);
        idleEvent.set(false);

        BlockingFilter second = blockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, second));

        Assert.assertTrue(filterModel.isRunning());
        Assert.assertFalse("Not idle while the new query is computed", idleEvent.get());
        first.permits.release();
        second.permits.release();
        // The thread stops running before it fires the event, so the event is awaited first
        await().atMost(10, TimeUnit.SECONDS).until(idleEvent::get);
        Assert.assertFalse(filterModel.isRunning());
    }

    private BlockingFilter blockingFilter() {
        BlockingFilter filter = new BlockingFilter();
        filters.add(filter);
        return filter;
    }
}
