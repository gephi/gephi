package org.gephi.io.processor.plugin;

import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.impl.ImportContainerImpl;

public class Utils {

    protected static class TestProcessor extends AbstractProcessor {

        public TestProcessor(GraphModel graphModel) {
            this.graphModel = graphModel;
            this.containers = new ImportContainerImpl[] {new ImportContainerImpl()};
        }

        public ImportContainerImpl getContainer() {
            return (ImportContainerImpl) containers[0];
        }

        @Override
        public void process() {

        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
