package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import org.gephi.graph.GraphGenerator;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * An exporter that can't write its output must fail, so the export isn't reported as a success.
 */
@RunWith(Parameterized.class)
public class WriteFailureTest {

    private final Supplier<CharacterExporter> exporterSupplier;

    public WriteFailureTest(String name, Supplier<CharacterExporter> exporterSupplier) {
        this.exporterSupplier = exporterSupplier;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> exporters() {
        return Arrays.asList(new Object[][] {
            {"CSV", (Supplier<CharacterExporter>) ExporterCSV::new},
            {"DL", (Supplier<CharacterExporter>) ExporterDL::new},
            {"GDF", (Supplier<CharacterExporter>) ExporterGDF::new},
            {"GEXF", (Supplier<CharacterExporter>) ExporterGEXF::new},
            {"GML", (Supplier<CharacterExporter>) ExporterGML::new},
            {"GraphML", (Supplier<CharacterExporter>) ExporterGraphML::new},
            {"Json", (Supplier<CharacterExporter>) ExporterJson::new},
            {"Pajek", (Supplier<CharacterExporter>) ExporterPajek::new},
            {"Spreadsheet", (Supplier<CharacterExporter>) ExporterSpreadsheet::new},
            {"VNA", (Supplier<CharacterExporter>) ExporterVNA::new},
        });
    }

    @Test(expected = RuntimeException.class)
    public void testWriteFailureIsThrown() {
        CharacterExporter exporter = exporterSupplier.get();
        exporter.setWorkspace(GraphGenerator.build().generateTinyGraph().getWorkspace());
        exporter.setWriter(new FailingWriter());
        exporter.execute();
    }

    private static class FailingWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("No space left on device");
        }

        @Override
        public void flush() throws IOException {
            throw new IOException("No space left on device");
        }

        @Override
        public void close() {
        }
    }
}
