/*
 Copyright 2008-2011 Gephi
 Authors : Jeremy Subtil
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

package org.gephi.preview.api;

/**
 * A canvas size, with a top left coordinate, a width and an heigth.
 *
 * @author Jeremy Subtil
 */
public class CanvasSize {

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    /**
     * Constructor.
     *
     * @param x      The x coordinate of the top left position
     * @param y      The y coordinate of the top left position
     * @param width  The canvas width
     * @param height The canvas height
     */
    public CanvasSize(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Constructs the default <code>CanvasSize</code>, with both width and
     * height equal to zero.
     */
    public CanvasSize() {
        this(0F, 0F, 0F, 0F);
    }

    /**
     * Returns the x coordinate of the top left position.
     *
     * @return the x coordinate of the top left position
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y coordinate of the top left position.
     *
     * @return the y coordinate of the top left position
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the canvas width.
     *
     * @return the canvas width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Returns the canvas height.
     *
     * @return the canvas height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Returns the x coordinate of the bottom right position.
     *
     * @return the x coordinate of the bottom right position
     */
    public float getMaxX() {
        return getX() + getWidth();
    }

    /**
     * Returns the y coordinate of the bottom right position.
     *
     * @return the y coordinate of the bottom right position
     */
    public float getMaxY() {
        return getY() + getHeight();
    }

    @Override
    public String toString() {
        return "CanvasSize{" +
            "x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            '}';
    }
}
