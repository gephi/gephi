package org.gephi.desktop.appearance.utils;

import java.util.Arrays;
import java.util.stream.Stream;
import org.gephi.appearance.AppearanceModelImpl;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.Transformer;
import org.gephi.desktop.appearance.AppearanceUIModel;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Node;

public class Utils {

    public static AppearanceUIModel newAppearanceUIModel() {
        GraphGenerator generator =
            GraphGenerator.build().withWorkspace().generateTinyGraph();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        model.getWorkspace().add(model);
        AppearanceUIModel uiModel = new AppearanceUIModel(model);
        model.getWorkspace().add(uiModel);
        return uiModel;
    }

    public static Function findNodeFunction(AppearanceUIModel model, Class<? extends Transformer> transformer) {
        Function[] functions = Stream.concat(Arrays.stream(model.getAppearanceModel().getNodeFunctions()),
            Arrays.stream(model.getAppearanceModel().getEdgeFunctions()))
            .toArray(Function[]::new);

        return Arrays.stream(functions).filter(
            f -> f.getElementClass().isAssignableFrom(Node.class) && f.getTransformer().getClass().equals(transformer))
            .findFirst().orElse(null);
    }
}
