package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.io.StringWriter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.junit.Assert;

public class Utils {

    public static void assertExporterMatch(String expectedFilename, CharacterExporter exporter) throws IOException {
        String expected = getResourceContent(expectedFilename);
        StringWriter writer = new StringWriter();
        exporter.setWriter(writer);
        exporter.execute();
        writer.close();
        Assert.assertEquals(cleanString(expected), cleanString(writer.toString()));
    }

    private static String cleanString(String str) {
        return str.replaceAll("[\\n\\t ]", "");
    }

    public static String getResourceContent(String fileName) throws IOException {
        return new String(Utils.class.getResourceAsStream(fileName)
            .readAllBytes());
    }
}
