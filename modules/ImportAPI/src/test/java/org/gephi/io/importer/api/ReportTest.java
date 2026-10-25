package org.gephi.io.importer.api;

import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;

public class ReportTest {

    @Test
    public void testMessageWithEmbeddedNewlineAndSemicolonRoundTrips() {
        Report report = new Report();
        String trickyMessage = "line one\nSOME;garbage;text that is not a level\nline three";
        report.log("before");
        report.log(trickyMessage);
        report.log("after");
        report.close();

        String text = report.getText();
        Assert.assertTrue(text.contains("before"));
        Assert.assertTrue(text.contains(trickyMessage));
        Assert.assertTrue(text.contains("after"));

        report.clean();
    }

    @Test
    public void testIssueWithEmbeddedNewlineAndSemicolonRoundTrips() {
        Report report = new Report();
        String trickyMessage = "🎯first line of the message\nWARNING;this looks like a level tag but isn't";
        report.logIssue(new Issue(trickyMessage, Issue.Level.INFO));
        report.close();

        Iterator<Issue> issues = report.getIssues(Integer.MAX_VALUE);
        Assert.assertTrue(issues.hasNext());
        Issue issue = issues.next();
        Assert.assertEquals(trickyMessage, issue.getMessage());
        Assert.assertEquals(Issue.Level.INFO, issue.getLevel());
        Assert.assertFalse(issues.hasNext());

        report.clean();
    }

    @Test
    public void testAppendWithMultipleEntriesAroundTrickyMessage() {
        Report main = new Report();
        Report sub = new Report();
        sub.log("sub message one");
        sub.logIssue(new Issue("a;b\nc", Issue.Level.WARNING));
        sub.log("sub message two");
        sub.close();

        main.log("main message");
        main.append(sub);

        String text = main.getText(true);
        Assert.assertTrue(text.contains("main message"));
        Assert.assertTrue(text.contains("sub message one"));
        Assert.assertTrue(text.contains("a;b\nc"));
        Assert.assertTrue(text.contains("sub message two"));

        main.clean();
        sub.clean();
    }
}
