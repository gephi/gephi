package org.gephi.io.importer.impl;

import java.awt.Color;
import java.util.Iterator;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.junit.Assert;
import org.junit.Test;

public class ElementDraftTest {

    @Test
    public void testColor() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");

        Assert.assertNull(edge.getColor());
        edge.setColor(Color.CYAN);
        Assert.assertEquals(Color.CYAN, edge.getColor());
        edge.setColor(255, 0, 0);
        Assert.assertEquals(Color.RED, edge.getColor());
        edge.setColor(0, 1f, 1f);
        Assert.assertEquals(Color.CYAN, edge.getColor());
        edge.setColor("red");
        Assert.assertEquals(Color.RED, edge.getColor());
        edge.setColor("#00FF00");
        Assert.assertEquals(Color.GREEN, edge.getColor());
        edge.setColor("0x0000FF");
        Assert.assertEquals(Color.BLUE, edge.getColor());
    }

    @Test
    public void testColorUnparsable() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setColor("foo");
        assertContainerIssues(edge.container.getReport(), Issue.Level.WARNING, "foo");
    }

    @Test
    public void testSetValue() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        Assert.assertNull(edge.getValue("foo"));
        edge.setValue("foo", "bar");
        Assert.assertEquals("bar", edge.getValue("foo"));
    }

    @Test
    public void testSetValueTimestampStringMap() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", "bar", 1.0);

        TimestampStringMap t = new TimestampStringMap();
        t.put(1.0, "bar");
        Assert.assertEquals(t, edge.getValue("foo"));
    }

    @Test
    public void testSetValueIntervalStringMap() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", "bar", 1.0, 2.0);

        IntervalStringMap i = new IntervalStringMap();
        i.put(new Interval(1.0, 2.0), "bar");
        Assert.assertEquals(i, edge.getValue("foo"));
    }

    @Test(expected = NullPointerException.class)
    public void testSetValueNull() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetValueNullIntervalStringMap() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", null, 1.0, 2.0);
    }

    @Test(expected = NullPointerException.class)
    public void testSetValueNullTimestampStringMap() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", null, 1.0);
    }

    @Test
    public void testSetValueDuplicateInterval() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", "bar", 1.0, 2.0);
        edge.setValue("foo", "hello", 1.0, 2.0);
        assertContainerIssues(edge.container.getReport(), Issue.Level.WARNING, "hello");
    }

    @Test
    public void testSetValueDuplicateTimestamp() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.setValue("foo", "bar", 1.0);
        edge.setValue("foo", "hello", 1.0);
        assertContainerIssues(edge.container.getReport(), Issue.Level.WARNING, "hello");
    }

    @Test
    public void testParseAndSetValue() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.container.addEdgeColumn("foo", String.class);
        edge.parseAndSetValue("foo", "bar");
        Assert.assertEquals("bar", edge.getValue("foo"));
    }

    @Test
    public void testParseAndSetValueEmpty() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.container.addEdgeColumn("foo", String.class);
        edge.parseAndSetValue("foo", "");
        Assert.assertNull(edge.getValue("foo"));
    }

    @Test
    public void testParseAndSetValueNull() {
        EdgeDraftImpl edge = new EdgeDraftImpl(new ImportContainerImpl(), "0");
        edge.container.addEdgeColumn("foo", String.class);
        edge.parseAndSetValue("foo", null);
        Assert.assertNull(edge.getValue("foo"));
    }


    // Utility

    private static void assertContainerIssues(Report report, Issue.Level level, String message) {
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
