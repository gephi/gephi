package org.gephi.appearance.utils;

import org.gephi.appearance.AppearanceModelImpl;
import org.gephi.graph.GraphGenerator;

public class Utils {

    public static AppearanceModelImpl newAppearanceModel() {
        GraphGenerator generator =
            GraphGenerator.build().withWorkspace().generateTinyGraph();
        return generator.getWorkspace().getLookup().lookup(AppearanceModelImpl.class);
    }
}
