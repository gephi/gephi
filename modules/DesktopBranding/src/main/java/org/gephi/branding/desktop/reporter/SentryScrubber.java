package org.gephi.branding.desktop.reporter;

import io.sentry.Breadcrumb;
import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.protocol.Message;
import io.sentry.protocol.SentryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes file system paths (which can contain user names, project or file names) from the
 * free-text parts of a Sentry event, such as exception messages.
 */
public final class SentryScrubber {

    // Absolute unix or windows path: at least one directory segment, not glued to a word, URL or
    // module name (e.g. "java.base/java.io.File"). Directories may contain spaces.
    private static final Pattern PATH = Pattern.compile(
        "(?<![\\w.:/\\\\-])(?:[A-Za-z]:[\\\\/]|[\\\\/])(?:[^\\\\/\\r\\n:*?\"<>|]+[\\\\/])+([^\\\\/\\r\\n:*?\"<>|()]*)");

    private SentryScrubber() {
    }

    public static SentryEvent beforeSend(SentryEvent event, Hint hint) {
        if (event.getExceptions() != null) {
            for (SentryException e : event.getExceptions()) {
                e.setValue(scrub(e.getValue()));
            }
        }
        Message message = event.getMessage();
        if (message != null) {
            message.setMessage(scrub(message.getMessage()));
            message.setFormatted(scrub(message.getFormatted()));
        }
        if (event.getBreadcrumbs() != null) {
            for (Breadcrumb b : event.getBreadcrumbs()) {
                b.setMessage(scrub(b.getMessage()));
            }
        }
        return event;
    }

    /**
     * Replaces every absolute path with {@code <path>}, keeping only the file extension.
     */
    static String scrub(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = PATH.matcher(ReportController.anonymizeLog(text));
        return m.replaceAll(r -> {
            String name = r.group(1).trim();
            int dot = name.lastIndexOf('.');
            String ext = dot > 0 ? name.substring(dot) : "";
            return Matcher.quoteReplacement("<path>" + ext + (r.group(1).endsWith(" ") ? " " : ""));
        });
    }
}
