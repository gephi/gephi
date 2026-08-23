package org.gephi.branding.desktop;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Test;

public class OutputHandlerTest {

    @Test
    public void testReentrantPublishIsIgnored() {
        AtomicInteger callCount = new AtomicInteger();
        Installer.OutputHandler handler = new Installer.OutputHandler() {
            @Override
            void doPublish(LogRecord record) {
                callCount.incrementAndGet();
                //Simulate the output write triggering a new log record on the
                //same thread, as happens with NetBeans' output window
                //bookkeeping (e.g. AbstractLines.checkLimits())
                publish(record);
            }
        };

        handler.publish(new LogRecord(Level.INFO, "test message"));

        Assert.assertEquals(1, callCount.get());
    }
}
