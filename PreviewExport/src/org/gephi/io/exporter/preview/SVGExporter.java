/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.exporter.preview;

import java.io.Writer;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.VectorExporter;

import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.Progress;
import org.openide.util.Lookup;
import org.w3c.dom.Document;

/**
 * Class exporting the preview graph as an SVG image.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SVGExporter implements CharacterExporter, VectorExporter, LongTask {

    //Architecture
    private Document doc;
    private ProgressTicket progress;
    private boolean cancel = false;
    private Workspace workspace;
    private Writer writer;
    private SVGTarget target;
    //Settings
    private boolean scaleStrokes = false;
    private float margin = 4;

    public boolean execute() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.refreshPreview(workspace);
        PreviewProperties props = controller.getModel(workspace).getProperties();
        props.putValue(SVGTarget.SCALE_STROKES, scaleStrokes);
        props.putValue(PreviewProperty.MARGIN, new Float((float) margin));
        target = (SVGTarget) controller.getRenderTarget(RenderTarget.SVG_TARGET, workspace);
        if (target instanceof LongTask) {
            ((LongTask) target).setProgressTicket(progress);
        }

        try {
            controller.render(target, workspace);

            // creates SVG-to-SVG transcoder
            SVGTranscoder t = new SVGTranscoder();
            t.addTranscodingHint(SVGTranscoder.KEY_XML_DECLARATION, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

            // sets transcoder input and output
            TranscoderInput input = new TranscoderInput(target.getDocument());

            // performs transcoding
            try {
                TranscoderOutput output = new TranscoderOutput(writer);
                t.transcode(input, output);
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Progress.finish(progress);

        return !cancel;
    }

    public boolean cancel() {
        cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setScaleStrokes(boolean scaleStrokes) {
        this.scaleStrokes = scaleStrokes;
    }

    public boolean isScaleStrokes() {
        return scaleStrokes;
    }
}
