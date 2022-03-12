package org.gephi.desktop.appearance;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.appearance.api.Function;
import org.openide.util.Lookup;

public class FunctionObserverExecutor implements Runnable {

    private static final long DEFAULT_DELAY = 1250;  //ms
    private final AppearanceUIModel model;
    private ScheduledExecutorService executor;

    public FunctionObserverExecutor(AppearanceUIModel model) {
        this.model = model;
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Appearance Function Observer"));
        executor.scheduleWithFixedDelay(this, getDelayInMs(), getDelayInMs(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = null;
    }

    @Override
    public void run() {
        try {
            Function selectedFunction = model.getSelectedFunction();
            if (selectedFunction != null && selectedFunction.hasChanged()) {
                Lookup.getDefault().lookup(AppearanceUIController.class).refreshFunction();
            }
        } catch (Exception e) {
            Logger.getLogger(TableObserverExecutor.class.getName())
                .log(Level.SEVERE, "Error while refreshing appearance's function", e);
        }
    }

    public boolean isRunning() {
        return executor != null;
    }

    private long getDelayInMs() {
        return DEFAULT_DELAY;
    }
}
