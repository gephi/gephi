/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.Vector;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RenderTargetBuilder.class)
public class G2DRenderTargetBuilder implements RenderTargetBuilder {

    @Override
    public RenderTarget buildRenderTarget(PreviewModel previewModel) {
        Integer width = previewModel.getProperties().getValue("width");
        Integer height = previewModel.getProperties().getValue("height");
        if (width != null && height != null) {
            width = Math.max(1, width);
            height = Math.max(1, height);
            return new G2DTargetImpl(previewModel, width, height);
        } else {
            return new G2DTargetImpl(previewModel, 1, 1);
        }
    }

    @Override
    public String getName() {
        return RenderTarget.G2D_TARGET;
    }

    public static class G2DTargetImpl extends AbstractRenderTarget implements G2DTarget {

        private final PreviewController previewController;
        private final PreviewModel previewModel;
        private G2DGraphics graphics;

        public G2DTargetImpl(PreviewModel model, int width, int height) {
            graphics = new G2DGraphics(width, height);
            previewController = Lookup.getDefault().lookup(PreviewController.class);
            previewModel = model;
        }

        @Override
        public void resize(int width, int height) {
            width = Math.max(1, width);
            height = Math.max(1, height);
            graphics.getGraphics().dispose();
            graphics = new G2DGraphics(width, height);
        }

        @Override
        public Graphics2D getGraphics() {
            return graphics.getGraphics();
        }

        @Override
        public Image getImage() {
            return graphics.getImage();
        }

        @Override
        public int getWidth() {
            return graphics.getWidth();
        }

        @Override
        public int getHeight() {
            return graphics.getHeight();
        }

        @Override
        public Vector getTranslate() {
            return graphics.getTranslate();
        }

        @Override
        public float getScaling() {
            return graphics.getScaling();
        }

        @Override
        public void setScaling(float scaling) {
            graphics.setScaling(scaling);
        }

        @Override
        public void setMoving(boolean moving) {
            previewModel.getProperties().putValue(PreviewProperty.MOVING, moving);
        }

        @Override
        public void reset() {
            graphics.reset();
        }

        @Override
        public void refresh() {
            if (graphics != null) {
                graphics.refresh(previewController.getModel(), this);
            }
        }
    }

    public static class G2DGraphics {

        private final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        private PreviewModel model;
        private boolean inited;
        //Drawing
        private final Image image;
        private final int width;
        private final int height;
        private final Graphics2D g2;
        private final Vector trans = new Vector();
        private float scaling;
        private Color background = Color.WHITE;

        public G2DGraphics(int width, int height) {
            this.width = width;
            this.height = height;
            GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            image = graphicsConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            g2 = (Graphics2D) image.getGraphics();

            //Smooth
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        }

        public void refresh(PreviewModel previewModel, RenderTarget target) {
            this.model = previewModel;
            if (model != null) {
                background = model.getProperties().getColorValue(PreviewProperty.BACKGROUND_COLOR);
                initAppletLayout();

                g2.clearRect(0, 0, width, height);
                g2.setTransform(new AffineTransform());

                if (background != null) {
                    g2.setColor(background);
                    g2.fillRect(0, 0, width, height);
                }

                // user zoom
                Vector center = new Vector(width / 2f, height / 2f);
                Vector scaledCenter = Vector.mult(center, scaling);
                Vector scaledTrans = Vector.sub(center, scaledCenter);
                g2.translate(scaledTrans.x, scaledTrans.y);
                g2.scale(scaling, scaling);

                // user move
                g2.translate(trans.x, trans.y);

                //Draw target
                previewController.render(target);
            }
        }

        public Vector getTranslate() {
            return trans;
        }

        public float getScaling() {
            return scaling;
        }

        public void setScaling(float scaling) {
            this.scaling = scaling;
        }

        public Graphics2D getGraphics() {
            return g2;
        }

        public Image getImage() {
            return image;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void reset() {
            inited = false;
        }

        /**
         * Initializes the preview applet layout according to the graph's
         * dimension.
         */
        private void initAppletLayout() {
//            graphSheet.setMargin(MARGIN);
            if (!inited && model != null && model.getDimensions() != null && model.getTopLeftPosition() != null) {

                // initializes zoom
                Dimension dimensions = model.getDimensions();
                Point topLeftPostition = model.getTopLeftPosition();
                Vector box = new Vector((float) dimensions.getWidth(), (float) dimensions.getHeight());
                float ratioWidth = width / box.x;
                float ratioHeight = height / box.y;
                scaling = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

                // initializes move
                Vector semiBox = Vector.div(box, 2);
                Vector topLeftVector = new Vector((float) topLeftPostition.x, (float) topLeftPostition.y);
                Vector center = new Vector(width / 2f, height / 2f);
                Vector scaledCenter = Vector.add(topLeftVector, semiBox);
                trans.set(center);
                trans.sub(scaledCenter);
//            lastMove.set(trans);

                inited = true;
            }
        }
    }
}
