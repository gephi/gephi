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
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDOMImplementation;
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
        int width = (int) previewModel.getDimensions().getWidth();
        int height = (int) previewModel.getDimensions().getHeight();
        width = Math.max(1, width);
        height = Math.max(1, height);
        int topLeftX = previewModel.getTopLeftPosition().x;
        int topLeftY = previewModel.getTopLeftPosition().y;
        boolean scaleStrokes = previewModel.getProperties().getBooleanValue(SVGTarget.SCALE_STROKES);

        SVGRenderTargetImpl renderTarget = new SVGRenderTargetImpl(width, height, topLeftX, topLeftY, scaleStrokes);
        return renderTarget;
    }

    @Override
    public String getName() {
        return RenderTarget.SVG_TARGET;
    }

    public static class SVGRenderTargetImpl extends AbstractRenderTarget implements SVGTarget {

        private Document document;
        private float scaleRatio = 1f;
        private Map<String, Element> topElements = new HashMap<String, Element>();

        public SVGRenderTargetImpl(int width, int height, int topLeftX, int topLeftY, boolean scaleStrokes) {
            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            DocumentType doctype = impl.createDocumentType(
                    "-//W3C//DTD SVG 1.1//EN",
                    "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
                    "");
            document = impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", doctype);

            // initializes CSS and SVG specific DOM interfaces
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            builder.build(ctx, document);

            //Dimension
            SupportSize supportSize = new SupportSize(595, 841, LengthUnit.PIXELS);
            if (width > height) {
                supportSize = new SupportSize(width * supportSize.getHeightInt() / height, supportSize.getHeightInt(), LengthUnit.PIXELS);
            } else if (height > width) {
                supportSize = new SupportSize(supportSize.getWidthInt(), height * supportSize.getWidthInt() / width, LengthUnit.PIXELS);
            }

            // root element
            Element svgRoot = document.getDocumentElement();
            svgRoot.setAttributeNS(null, "width", supportSize.getWidth());
            svgRoot.setAttributeNS(null, "height", supportSize.getHeight());
            svgRoot.setAttributeNS(null, "version", "1.1");
            svgRoot.setAttributeNS(null, "viewBox", String.format(Locale.ENGLISH, "%d %d %d %d",
                    topLeftX,
                    topLeftY,
                    width,
                    height));

            //Scale & ratio
            if (scaleStrokes) {
                scaleRatio = supportSize.getWidthInt() / (float) width;
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
     * @author Jérémy Subtil <jeremy.subtil@gephi.org>
     */
    public static class SupportSize {

        private final Integer width;
        private final Integer height;
        private final LengthUnit lengthUnit;

        /**
         * Constructor.
         *
         * @param width       the support's width
         * @param height      the support's height
         * @param lengthUnit  the lenght unit
         */
        public SupportSize(int width, int height, LengthUnit lengthUnit) {
            this.width = width;
            this.height = height;
            this.lengthUnit = lengthUnit;
        }

        public Integer getWidthInt() {
            return width;
        }

        public Integer getHeightInt() {
            return height;
        }

        /**
         * Returns the support's width.
         *
         * @return the support's width
         */
        public String getWidth() {
            return width.toString() + lengthUnit.toString();
        }

        /**
         * Returns the support's height.
         *
         * @return the support's height
         */
        public String getHeight() {
            return height.toString() + lengthUnit.toString();
        }
    }

    /**
     * Enum representing a set of lenght units.
     *
     * @author Jérémy Subtil <jeremy.subtil@gephi.org>
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
