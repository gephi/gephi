package org.gephi.desktop.appearance;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TableObserver;
import org.openide.util.Lookup;

public class TableObserverExecutor implements Runnable {

    private static final long DEFAULT_DELAY = 1000;  //ms
    private final AppearanceUIModel model;
    private ScheduledExecutorService executor;
    private TableObserver nodeTableObserver;
    private TableObserver edgeTableObserver;

    public TableObserverExecutor(AppearanceUIModel model) {
        this.model = model;
    }

    public void start() {
        GraphModel graphModel = model.appearanceModel.getGraphModel();
        nodeTableObserver = graphModel.getNodeTable().createTableObserver(false);
        edgeTableObserver = graphModel.getEdgeTable().createTableObserver(false);

        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Appearance Table Observer"));
        executor.scheduleWithFixedDelay(this, 0, getDelayInMs(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        synchronized (this) {
            if (nodeTableObserver != null) {
                nodeTableObserver.destroy();
                nodeTableObserver = null;
            }
            if (edgeTableObserver != null) {
                edgeTableObserver.destroy();
                edgeTableObserver = null;
            }
        }
        executor = null;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                String selectedElementClass = model.selectedElementClass;
                if (nodeTableObserver != null && selectedElementClass.equals(AppearanceUIController.NODE_ELEMENT)) {
                    if (nodeTableObserver.hasTableChanged()) {
                        Lookup.getDefault().lookup(AppearanceUIController.class).refreshColumnsList();
                    }
                } else if (edgeTableObserver != null &&
                    selectedElementClass.equals(AppearanceUIController.EDGE_ELEMENT)) {
                    if (edgeTableObserver.hasTableChanged()) {
                        Lookup.getDefault().lookup(AppearanceUIController.class).refreshColumnsList();
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(TableObserverExecutor.class.getName())
                    .log(Level.SEVERE, "Error while refreshing appearance's column list", e);
            }
        }
    }

    public boolean isRunning() {
        return executor != null;
    }

    private long getDelayInMs() {
        return DEFAULT_DELAY;
    }
}