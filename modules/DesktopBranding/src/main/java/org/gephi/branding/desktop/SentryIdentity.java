package org.gephi.branding.desktop;

import io.sentry.util.UUIDGenerator;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public final class SentryIdentity {
    private static final String KEY = "sentry.distinctId";

    public static String getOrCreateDistinctId() {
        Preferences prefs = NbPreferences.forModule(SentryIdentity.class);
        String id = prefs.get(KEY, null);
        if (id == null || id.isBlank()) {
            id = UUIDGenerator.randomUUID().toString();
            prefs.put(KEY, id);
        }
        return id;
    }
}
