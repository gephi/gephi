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
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.VectorExporter;

import org.gephi.preview.api.PreviewController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


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
    //Settings
    private final static float MARGIN = 25f;
    private boolean scaleStrokes = false;
    //Helper

    private Element svgRoot;
    private Element nodeGroupElem;
    private Element edgeGroupElem;
    private Element labelGroupElem;
    private Element labelBorderGroupElem;
    private float scaleRatio = 1f;

    public boolean execute() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);

        try {
//            exportData(graphSheet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return !cancel;
    }

 

    public boolean cancel() {
        cancel = true;
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
