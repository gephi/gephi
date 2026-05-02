package org.gephi.viz.engine.jogl.pipeline.common;

import jogamp.text.TextRenderer;
import org.gephi.viz.engine.jogl.pipeline.text.AbstractLabelData;
import org.gephi.viz.engine.spi.WorldData;

public class LabelWorldData implements WorldData {

    private final TextRenderer textRenderer;
    private final AbstractLabelData.LabelBatch[] labelBatches;
    private final int maxIndex;

    public LabelWorldData(TextRenderer textRenderer, AbstractLabelData.LabelBatch[] labelBatches, int maxIndex) {
        this.textRenderer = textRenderer;
        this.labelBatches = labelBatches;
        this.maxIndex = maxIndex;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public AbstractLabelData.LabelBatch[] getLabelBatches() {
        return labelBatches;
    }

    public int getMaxIndex() {
        return maxIndex;
    }
}
