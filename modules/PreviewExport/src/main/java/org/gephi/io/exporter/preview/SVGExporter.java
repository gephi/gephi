/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
 * @author Jérémy Subtil
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
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
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
                props.removeSimpleValue(PreviewProperty.MARGIN);
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
