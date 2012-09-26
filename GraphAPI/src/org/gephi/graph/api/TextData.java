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
package org.gephi.graph.api;

import java.awt.Color;

/**
 * Contains all extended data related to text display.
 *
 * @author Mathieu Bastian
 */
public interface TextData {

    /**
     * Returns text width
     * @return Text width
     */
    public float getWidth();

    /**
     * Returns text height
     * @return Text heigh
     */
    public float getHeight();

    /**
     * Returns the text <code>String</code>
     * @return Text <code>String</code>
     */
    public String getText();

    /**
     * Returns text size
     * @return Text size
     */
    public float getSize();

     /**
     * Get red component of the text color.
     * Value ranges from 0.0 to 1.0
     * @return Red color component
     */
    public float getR();

    /**
     * Get green component of the text color.
     * Value ranges from 0.0 to 1.0
     * @return Green color component
     */
    public float getG();

    /**
     * Get blue component of the text color.
     * Value ranges from 0.0 to 1.0
     * @return Blue color component
     */
    public float getB();

    /**
     * Returns alpha color value (transparency) of the text.
     * Value ranges from 0.0 to 1.0
     * @return Alpha (transparency)
     */
    public float getAlpha();

    /**
     * Returns visibility of the text
     * @return True if visible, false otherwise
     */
    public boolean isVisible();

    /**
     * Sets the text <code>String</code>
     * @param text Text <code>String</code>
     */
    public void setText(String text);

    /**
     * Sets all color components for the text.
     * All values should be between 0.0 and 1.0
     * @param r Red color component
     * @param g Green color component
     * @param b Blue color component
     * @param alpha Alpha (transparency)
     */
    public void setColor(float r, float g, float b, float alpha);

    /**
     * Sets text color
     * @param color Text color
     */
    public void setColor(Color color);

    /**
     * Sets text size
     * @param size Text size
     */
    public void setSize(float size);

    /**
     * Sets visible state of the text
     * @param visible Text visibility
     */
    public void setVisible(boolean visible);
}
