/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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
package org.gephi.preview;

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RenderTargetBuilder.class)
public class SVGRenderTargetBuilder implements RenderTargetBuilder {

    @Override
    public RenderTarget buildRenderTarget(PreviewModel previewModel) {
        CanvasSize cs = previewModel.getGraphicsCanvasSize();
        boolean scaleStrokes = previewModel.getProperties()
                .getBooleanValue(SVGTarget.SCALE_STROKES);

        SVGRenderTargetImpl renderTarget
                = new SVGRenderTargetImpl(cs, scaleStrokes);
        return renderTarget;
    }

    @Override
    public String getName() {
        return RenderTarget.SVG_TARGET;
    }

    public static class SVGRenderTargetImpl
            extends AbstractRenderTarget implements SVGTarget {

        private final Document document;
        private float scaleRatio = 1f;
        private final Map<String, Element> topElements = new HashMap<>();

        public SVGRenderTargetImpl(CanvasSize cs, boolean scaleStrokes) {
            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            DocumentType doctype = impl.createDocumentType(
                    "svg",
                    "-//W3C//DTD SVG 1.1//EN",
                    "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd");
            document = impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", doctype);

            // initializes CSS and SVG specific DOM interfaces
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            builder.build(ctx, document);

            //Dimension
            SupportSize ss = new SupportSize(595F, 841F, LengthUnit.PIXELS);
            if (cs.getWidth() > cs.getHeight()) {
                ss = new SupportSize(
                        cs.getWidth() * ss.getHeightFloat() / cs.getHeight(),
                        ss.getHeightFloat(),
                        LengthUnit.PIXELS);
            } else if (cs.getHeight() > cs.getWidth()) {
                ss = new SupportSize(
                        ss.getWidthFloat(),
                        cs.getHeight() * ss.getWidthFloat() / cs.getWidth(),
                        LengthUnit.PIXELS);
            }

            // root element
            Element svgRoot = document.getDocumentElement();
            svgRoot.setAttributeNS(null, "width", cs.getWidth() + "");
            svgRoot.setAttributeNS(null, "height", cs.getHeight() + "");
            svgRoot.setAttributeNS(null, "version", "1.1");
            svgRoot.setAttributeNS(
                    null,
                    "viewBox",
                    String.format(Locale.ENGLISH, "%f %f %f %f",
                            cs.getX(),
                            cs.getY(),
                            cs.getWidth(),
                            cs.getHeight()));

            //Scale & ratio
            if (scaleStrokes) {
                scaleRatio = ss.getWidthFloat() / cs.getWidth();
            }
        }

        @Override
        public Element getTopElement(String name) {
            Element topElement = topElements.get(name);
            if (topElement == null) {
                topElement = createElement("g");
                topElement.setAttribute("id", name);
                topElements.put(name, topElement);
                document.getDocumentElement().appendChild(topElement);
            }
            return topElement;
        }

        @Override
        public Document getDocument() {
            return document;
        }

        @Override
        public float getScaleRatio() {
            return scaleRatio;
        }

        @Override
        public Element createElement(String qualifiedName) {
            return document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, qualifiedName);
        }

        @Override
        public Text createTextNode(String data) {
            return document.createTextNode(data);
        }

        @Override
        public String toHexString(Color color) {
            String str = Integer.toHexString(color.getRGB());

            for (int i = str.length(); i > 6; i--) {
                str = str.substring(1);
            }

            for (int i = str.length(); i < 6; i++) {
                str = "0" + str;
            }

            return "#" + str;
        }
    }

    /**
     * Implementation of the size of an export support.
     *
     * @author Jérémy Subtil
     */
    public static class SupportSize {

        private final float width;
        private final float height;
        private final LengthUnit lengthUnit;

        /**
         * Constructor.
         *
         * @param width       the support's width
         * @param height      the support's height
         * @param lengthUnit  the lenght unit
         */
        public SupportSize(float width, float height, LengthUnit lengthUnit) {
            this.width = width;
            this.height = height;
            this.lengthUnit = lengthUnit;
}

        public float getWidthFloat() {
            return width;
        }

        public float getHeightFloat() {
            return height;
        }

        /**
         * Returns the support's width.
         *
         * @return the support's width
         */
        public String getWidth() {
            return width + lengthUnit.toString();
        }

        /**
         * Returns the support's height.
         *
         * @return the support's height
         */
        public String getHeight() {
            return height + lengthUnit.toString();
        }
    }

    /**
     * Enum representing a set of lenght units.
     *
     * @author Jérémy Subtil
     */
    public enum LengthUnit {

        CENTIMETER,
        MILLIMETER,
        INCH,
        PIXELS,
        PERCENTAGE;

        @Override
        public String toString() {
            switch (this) {
                case CENTIMETER:
                    return "cm";
                case MILLIMETER:
                    return "mm";
                case INCH:
                    return "in";
                case PIXELS:
                    return "px";
                default:
                case PERCENTAGE:
                    return "%";
            }
        }
    }
}
