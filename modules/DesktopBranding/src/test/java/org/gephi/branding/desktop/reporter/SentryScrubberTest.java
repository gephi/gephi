package org.gephi.branding.desktop.reporter;

import org.junit.Assert;
import org.junit.Test;

public class SentryScrubberTest {

    @Test
    public void testUnixFileNotFound() {
        Assert.assertEquals("<path>.gexf (No such file or directory)",
            SentryScrubber.scrub("/Users/jane/My Research/secret graph.gexf (No such file or directory)"));
    }

    @Test
    public void testWindowsFileNotFound() {
        Assert.assertEquals("<path>.csv (The system cannot find the file specified)",
            SentryScrubber.scrub("C:\\Users\\jane\\Docs\\edges.csv (The system cannot find the file specified)"));
    }

    @Test
    public void testNonPathsUntouched() {
        String text = "java.base/java.io.FileInputStream.open0 failed, see https://gephi.org/a/b and and/or";
        Assert.assertEquals(text, SentryScrubber.scrub(text));
    }

    @Test
    public void testNull() {
        Assert.assertNull(SentryScrubber.scrub(null));
    }
}
