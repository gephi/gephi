package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.junit.Assert;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

public class Utils {

    public static void print(CharacterExporter exporter) throws IOException {
        StringWriter writer = new StringWriter();
        exporter.setWriter(writer);
        exporter.execute();
        writer.close();
        System.out.println(writer);
    }

    public static void assertExporterMatch(String expectedFilename, CharacterExporter exporter) throws IOException {
        String expected = getResourceContent(expectedFilename);
        StringWriter writer = new StringWriter();
        exporter.setWriter(writer);
        exporter.execute();
        writer.close();

        if (expected.startsWith("<?xml")) {
            Diff myDiff = DiffBuilder.compare(expected).checkForIdentical().ignoreComments().ignoreWhitespace()
                .withTest(writer.toString()).build();

            Iterator<Difference> iter = myDiff.getDifferences().iterator();
            int size = 0;
            while (iter.hasNext()) {
                System.out.println("Difference: " + iter.next().toString());
                size++;
            }
            Assert.assertEquals(0, size);
        } else {
            Assert.assertEquals(cleanString(expected), cleanString(writer.toString()));
        }
    }

    private static String cleanString(String str) {
        return str.replaceAll("[\\r]", "");
    }

    public static String getResourceContent(String fileName) throws IOException {
        return new String(Utils.class.getResourceAsStream(fileName)
            .readAllBytes());
    }
}
