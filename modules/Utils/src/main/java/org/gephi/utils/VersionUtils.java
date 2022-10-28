package org.gephi.utils;

import org.openide.util.NbBundle;

public class VersionUtils {

    /**
     * Returns the current version of the Gephi application.
     *
     * @return gephi version
     */
    public static String getGephiVersion() {
        return NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion")
            .replaceAll("( [0-9]{12})$", "");
    }
}
