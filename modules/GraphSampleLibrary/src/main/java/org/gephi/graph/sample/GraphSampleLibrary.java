package org.gephi.graph.sample;

import java.util.List;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ISampleGraphLibrary.class)
public class GraphSampleLibrary implements ISampleGraphLibrary {
    @Override
    public List<FileSample> getFileSamples() {
        return List.of(
            new FileSample("TEST", "TEST", "TEST", "TEST")
        );
    }
}
