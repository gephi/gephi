/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
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

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.project.api.Workspace;

public class PNGExporter implements VectorExporter, ByteExporter {

    private boolean exportVisible = false;
    private Workspace workspace;
    private PDFExporter pdfExporter = new PDFExporter();
    private OutputStream stream;
    private int width = 1024;
    private int height = 1024;
    private int bottomMargin = 20;
    private int topMargin = 20;
    private int leftMargin = 20;
    private int rightMargin = 20;

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isExportVisible() {
        return exportVisible;
    }

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public boolean execute() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        pdfExporter.setWorkspace(workspace);
        pdfExporter.setOutputStream(byteStream);

        pdfExporter.setMarginBottom(bottomMargin);
        pdfExporter.setMarginTop(topMargin);
        pdfExporter.setMarginLeft(leftMargin);
        pdfExporter.setMarginRight(rightMargin);
        pdfExporter.setPageSize(new com.itextpdf.text.Rectangle(width, height));

        pdfExporter.execute();

        try {
            export(byteStream.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(PNGExporter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    public void export(byte[] pdf) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(pdf);
        PDFFile file = new PDFFile(buf);
        PDFPage page = file.getPage(1);

        Rectangle rect = new Rectangle(0, 0, width, height);

        BufferedImage img = (BufferedImage) page.getImage(width, height, rect, null, true, true);

        ImageIO.write(img, "png", stream);
    }

    @Override
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }
}
