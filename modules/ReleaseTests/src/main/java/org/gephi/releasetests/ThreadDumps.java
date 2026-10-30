package org.gephi.releasetests;

import java.io.File;
import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Thread dumps of this JVM, with full stacks and the locks each thread holds or waits for. The read holds of a
 * read-write lock, like the graph lock, aren't owned by a thread and never show up.
 */
final class ThreadDumps {

    private ThreadDumps() {
    }

    /**
     * Writes a thread dump under <code>dumps</code> in the output directory and returns its path relative to it, or
     * <code>null</code> if it couldn't be written.
     */
    static String write(Results results, String reason) {
        File file = results.newFile("dumps", reason + ".txt");
        try {
            Files.writeString(file.toPath(), dump(), StandardCharsets.UTF_8);
            return results.relativePath(file);
        } catch (IOException | RuntimeException ex) {
            System.err.println("Release tests can't write a thread dump: " + ex);
            return null;
        }
    }

    static String dump() {
        StringBuilder builder = new StringBuilder();
        for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
            builder.append('"').append(info.getThreadName()).append("\" #").append(info.getThreadId())
                .append(info.isDaemon() ? " daemon " : " ").append(info.getThreadState());
            if (info.getLockName() != null) {
                builder.append(" on ").append(info.getLockName());
            }
            if (info.getLockOwnerName() != null) {
                builder.append(" owned by \"").append(info.getLockOwnerName()).append("\" #")
                    .append(info.getLockOwnerId());
            }
            builder.append('\n');
            StackTraceElement[] stack = info.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                builder.append("\tat ").append(stack[i]).append('\n');
                for (MonitorInfo monitor : info.getLockedMonitors()) {
                    if (monitor.getLockedStackDepth() == i) {
                        builder.append("\t- locked ").append(monitor).append('\n');
                    }
                }
            }
            LockInfo[] synchronizers = info.getLockedSynchronizers();
            if (synchronizers.length > 0) {
                builder.append("\tLocked synchronizers:\n");
                for (LockInfo synchronizer : synchronizers) {
                    builder.append("\t- ").append(synchronizer).append('\n');
                }
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
