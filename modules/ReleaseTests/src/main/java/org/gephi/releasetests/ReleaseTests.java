package org.gephi.releasetests;

import java.io.File;

/**
 * The system properties the supervisor starts Gephi with to run a release test suite.
 */
final class ReleaseTests {

    /**
     * Name of the suite to run. Without it, this module does nothing.
     */
    static final String SUITE_PROPERTY = "gephi.releasetests.suite";

    /**
     * Directory the results, thread dumps and other files are written to.
     */
    static final String OUT_PROPERTY = "gephi.releasetests.out";

    private ReleaseTests() {
    }

    static String suite() {
        return System.getProperty(SUITE_PROPERTY);
    }

    static File outDir() {
        String out = System.getProperty(OUT_PROPERTY);
        if (out != null) {
            return new File(out);
        }
        return new File(System.getProperty("netbeans.user"), "var" + File.separator + "release-tests");
    }
}
