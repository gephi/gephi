package org.gephi.viz.engine.jogl.pipeline.common;

import jogamp.text.TextRenderer;
import org.gephi.viz.engine.jogl.pipeline.text.NodeLabelData;
import org.gephi.viz.engine.spi.WorldData;

public record NodeLabelWorldData(TextRenderer textRenderer, NodeLabelData.LabelBatch[] labelBatches, int maxIndex)
    implements WorldData {

}
