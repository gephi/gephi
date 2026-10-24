package org.gephi.releasetests;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Turns log records at {@link Level#SEVERE} or above into violations. That covers exceptions reported with
 * <code>Exceptions.printStackTrace()</code>, which are logged above SEVERE, and uncaught exceptions, which the
 * platform logs. {@link InstallLogRecorder} installs it as soon as this module starts, to catch failures in other
 * modules' startup too.
 * <p>
 * Only the first {@value #MAX_VIOLATIONS} records become violations, so that an error logged on every frame doesn't
 * flood the results. The <code>severeLogRecords</code> metric counts them all.
 */
final class LogRecorder extends Handler {

    static final int MAX_VIOLATIONS = 20;

    private final Results results;
    private final SimpleFormatter formatter = new SimpleFormatter();
    private final AtomicInteger count = new AtomicInteger();

    LogRecorder(Results results) {
        this.results = results;
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null || record.getLevel().intValue() < Level.SEVERE.intValue()) {
            return;
        }
        int n = count.incrementAndGet();
        results.metric("severeLogRecords", n);
        if (n > MAX_VIOLATIONS) {
            return;
        }
        String text = null;
        try {
            text = record.getMessage() != null ? formatter.formatMessage(record) : null;
        } catch (RuntimeException ex) {
            text = record.getMessage();
        }
        Throwable thrown = record.getThrown();
        StringBuilder message = new StringBuilder();
        message.append(record.getLevel().getName()).append(' ').append(record.getLoggerName()).append(": ")
            .append(text != null ? text : thrown != null ? thrown.toString() : "");
        if (thrown != null) {
            message.append('\n').append(Results.stackTrace(thrown));
        }
        if (n == MAX_VIOLATIONS) {
            message.append("\nFurther records are only counted in the severeLogRecords metric");
        }
        results.violation("log", message.toString(), null);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
