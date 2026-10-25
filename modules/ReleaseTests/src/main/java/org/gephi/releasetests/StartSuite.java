package org.gephi.releasetests;

import org.openide.windows.OnShowing;

/**
 * Starts the suite to run, if any, on its own thread once the main window shows.
 */
@OnShowing
public class StartSuite implements Runnable {

    @Override
    public void run() {
        if (ReleaseTests.suite() != null) {
            new Thread(new Driver(), "Release Tests Driver").start();
        }
    }
}
