/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.gephi.desktop.welcome.WelcomeTopComponent;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
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

        String sample = "/org/gephi/desktop/welcome/samples/Java.gexf";
        final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(sample);
        try {
            stream.reset();
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FileImporter fileImporter = importController.getFileImporter(".gexf");
        Container container = importController.importFile(stream, fileImporter);

        importController.process(container, new DefaultProcessor(), workspace);

        //Set label edges
//        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
//        for (Edge edge : graphController.getModel().getGraph().getEdges()) {
//            edge.getEdgeData().setLabel("Label test");
//        }

        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();

        PreviewProperties props = model.getProperties();
        props.putValue(PreviewProperty.SHOW_NODE_LABELS, false);
        props.putValue(PreviewProperty.EDGE_OPACITY, 20f);

        PDFExporter pDFExporter = new PDFExporter();
        pDFExporter.setLandscape(true);

        pDFExporter.setWorkspace(workspace);
        try {
            File file = new File("/Users/mbastian/test.pdf");
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
