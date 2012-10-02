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


/**
 * All graphic attributes a renderable element can have.
 * Renderable elements are elements such as nodes and edges.
 * @author Mathieu Bastian
 */
public interface Renderable extends Spatial {

    /**
     * Set x coordinate for the element
     * @param x Value of the x coordinate
     */
    public void setX(float x);

    /**
     * Set y coordinate for the element
     * @param y Value of the y coordinate
     */
    public void setY(float y);

    /**
     * Set z coordinate for the element
     * @param z Value of the z coordinate
     */
    public void setZ(float z);

    /**
     * Returns radius of the element
     * @return Radius
     */
    public float getRadius();

    /**
     * Returns size of the element.
     * @return Size
     */
    public float getSize();

    /**
     * Set size for the element
     * @param size Size
     */
    public void setSize(float size);

    /**
     * Get red component of the element color.
     * Value ranges from 0.0 to 1.0
     * @return Red color component
     */
    public float r();

    /**
     * Get green component of the element color.
     * Value ranges from 0.0 to 1.0
     * @return Green color component
     */
    public float g();

    /**
     * Get blue component of the element color.
     * Value ranges from 0.0 to 1.0
     * @return Blue color component
     */
    public float b();

    /**
     * Sets red color component for the element.
     * Value should be between 0.0 and 1.0
     * @param r Red color component
     */
    public void setR(float r);

    /**
     * Sets green color component for the element.
     * Value should be between 0.0 and 1.0
     * @param r Green color component
     */
    public void setG(float g);

    /**
     * Sets blue color component for the element.
     * Value should be between 0.0 and 1.0
     * @param r Blue color component
     */
    public void setB(float b);

    /**
     * Sets all color components for the element.
     * All values should be between 0.0 and 1.0
     * @param r Red color component
     * @param g Green color component
     * @param b Blue color component
     */
    public void setColor(float r, float g, float b);

    /**
     * Returns alpha color value (transparency) of the element.
     * Value ranges from 0.0 to 1.0
     * @return Alpha (transparency)
     */
    public float alpha();

    /**
     * Sets alpha (transparency) color component for the element.
     * Value should be between 0.0 and 1.0
     * @param alpha Alpha (transparency)
     */
    public void setAlpha(float alpha);

    /**
     * Returns graphical 2D/3D model related to the element
     * @return Graphical model
     */
    public Model getModel();

    /**
     * Sets graphical 2D/3D model assigned to the element
     * @param obj 
     */
    public void setModel(Model obj);

    /**
     * Returns <code>TextData</code> instance assigned to the element.
     * @return 
     */
    public TextData getTextData();
}
