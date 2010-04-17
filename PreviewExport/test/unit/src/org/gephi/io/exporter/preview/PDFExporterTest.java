/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.preview;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

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
        PDFExporter pDFExporter = new PDFExporter();
        try {
            pDFExporter.exportData(new File("test2.pdf"), null);
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
