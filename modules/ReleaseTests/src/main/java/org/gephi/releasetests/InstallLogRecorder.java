package org.gephi.releasetests;

import java.util.logging.Logger;
import org.openide.modules.OnStart;

/**
 * Installs the {@link LogRecorder} on the root logger as soon as this module starts, when a suite is to run.
 */
@OnStart
public class InstallLogRecorder implements Runnable {

    @Override
    public void run() {
        if (ReleaseTests.suite() != null) {
            Logger.getLogger("").addHandler(new LogRecorder(Results.get()));
        }
    }
}
