package org.gephi.releasetests;

/**
 * A scenario run in Gephi by the release tests, as a series of steps.
 */
interface Suite {

    void run(SuiteContext context) throws Exception;
}
