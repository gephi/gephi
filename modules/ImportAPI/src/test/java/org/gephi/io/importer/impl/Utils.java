package org.gephi.io.importer.impl;

import java.util.Iterator;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.junit.Assert;

public class Utils {

    public static void assertContainerIssues(Report report, Issue.Level level, String message) {
        report.close();
        boolean found = false;
        Iterator<Issue> issues = report.getIssues(1);
        while (issues.hasNext()) {
            Issue issue = issues.next();
            if (issue.getLevel().equals(level) && issue.getMessage().contains(message)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }
}
