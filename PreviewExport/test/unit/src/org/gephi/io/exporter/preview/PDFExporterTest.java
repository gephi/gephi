/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PDFExporterTest {

    public PDFExporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testExport() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        RandomGraph randomGraph = new RandomGraph();
        randomGraph.generate(container.getLoader());

        //Append container to graph structure
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);

        //Set labels
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = gc.getModel(workspace);
        for (Node n : graphModel.getGraph().getNodes()) {
            n.getNodeData().setLabel("Node " + n.getNodeData().getId());
        }

        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        Font font = new Font("Broadway", Font.PLAIN, 15);
        model.getNodeSupervisor().setBaseNodeLabelFont(font);
        model.getNodeSupervisor().setShowNodeLabels(Boolean.TRUE);
        Lookup.getDefault().lookup(PreviewController.class).setBackgroundColor(Color.GRAY);

        PDFExporter pDFExporter = new PDFExporter();

        pDFExporter.setWorkspace(workspace);
        try {
            File file = new File("test.pdf");
            System.out.println(file.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(file);
            pDFExporter.setOutputStream(fos);
            pDFExporter.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            Exceptions.printStackTrace(ex);
        }
    }
    /**
     * Test of exportData method, of class PDFExporter.
     */
    /* @Test
    public void testExportData() throws Exception {
    File file = new File("test.pdf");

    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    DocumentType doctype = impl.createDocumentType(
    "-//W3C//DTD SVG 1.1//EN",
    "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
    "");
    String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
    float MARGIN = 25f;
    Document doc = impl.createDocument(namespaceURI, "svg", doctype);
    SupportSize supportSize = new SupportSize(210, 297, LengthUnit.MILLIMETER);

    // initializes CSS and SVG specific DOM interfaces
    UserAgent userAgent = new UserAgentAdapter();
    DocumentLoader loader = new DocumentLoader(userAgent);
    BridgeContext ctx = new BridgeContext(userAgent, loader);
    ctx.setDynamicState(BridgeContext.DYNAMIC);
    GVTBuilder builder = new GVTBuilder();
    builder.build(ctx, doc);
    Element svgRoot;

    // root element
    svgRoot = doc.getDocumentElement();
    svgRoot.setAttributeNS(null, "width", supportSize.getWidth());
    svgRoot.setAttributeNS(null, "height", supportSize.getHeight());
    svgRoot.setAttributeNS(null, "version", "1.1");
    svgRoot.setAttributeNS(null, "viewBox", String.format(Locale.ENGLISH, "%d %d %d %d", -3323, -3057, 7379, 5977));

    Element labelGroupElem = doc.createElementNS(namespaceURI, "g");
    labelGroupElem.setAttribute("id", "labels");
    svgRoot.appendChild(labelGroupElem);

    for (int i = 0; i < 500; i++) {
    Random rd = new Random();
    char[] ch = new char[10];
    for (int j = 0; j < ch.length; j++) {
    ch[j] = (char) rd.nextInt(255);
    }
    String str = new String(ch);

    Text labelText = doc.createTextNode(str);

    Element labelElem = doc.createElementNS(namespaceURI, "text");
    labelElem.setAttribute("x", "" + (Math.random() * 1000));
    labelElem.setAttribute("y", "" + (Math.random() * 1000));
    labelElem.setAttribute("style", "text-anchor: middle");
    labelElem.setAttribute("fill", "#0000ff");
    labelElem.setAttribute("font-family", "SansSerif");
    labelElem.setAttribute("font-size", "8");
    labelElem.appendChild(labelText);
    labelGroupElem.appendChild(labelElem);
    }


    try {
    OutputStream ostream = null;
    PDFTranscoder t = new PDFTranscoder();
    TranscoderInput input = new TranscoderInput(doc);

    // performs transcoding
    try {
    ostream = new BufferedOutputStream(new FileOutputStream(file));
    TranscoderOutput output = new TranscoderOutput(ostream);

    t.transcode(input, output);

    } finally {
    ostream.close();
    }

    } catch (Exception e) {
    e.printStackTrace();
    }
    }*/
}
