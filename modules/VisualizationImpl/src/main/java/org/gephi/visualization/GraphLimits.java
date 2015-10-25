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
package org.gephi.visualization;

import org.gephi.lib.gleem.linalg.Vec3f;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphLimits {

    private float minXoctree;
    private float maxXoctree;
    private float minYoctree;
    private float maxYoctree;
    private float minZoctree;
    private float maxZoctree;
    private Vec3f closestPoint = new Vec3f(0, 0, 0);
    private float maxWeight;
    private float minWeight;

    public synchronized float getMaxXoctree() {
        return maxXoctree;
    }

    public synchronized void setMaxXoctree(float maxXoctree) {
        this.maxXoctree = maxXoctree;
    }

    public synchronized float getMaxYoctree() {
        return maxYoctree;
    }

    public synchronized void setMaxYoctree(float maxYoctree) {
        this.maxYoctree = maxYoctree;
    }

    public synchronized float getMaxZoctree() {
        return maxZoctree;
    }

    public synchronized void setMaxZoctree(float maxZoctree) {
        this.maxZoctree = maxZoctree;
    }

    public synchronized float getMinXoctree() {
        return minXoctree;
    }

    public synchronized void setMinXoctree(float minXoctree) {
        this.minXoctree = minXoctree;
    }

    public synchronized float getMinYoctree() {
        return minYoctree;
    }

    public synchronized void setMinYoctree(float minYoctree) {
        this.minYoctree = minYoctree;
    }

    public synchronized float getMinZoctree() {
        return minZoctree;
    }

    public synchronized void setMinZoctree(float minZoctree) {
        this.minZoctree = minZoctree;
    }

    public synchronized float getDistanceFromPoint(float x, float y, float z) {

        float dis = (float) Math.sqrt((closestPoint.x() - x) * (closestPoint.x() - x) + (closestPoint.y() - y) * (closestPoint.y() - y) + (closestPoint.z() - z) * (closestPoint.z() - z));
        return dis;

        //Minimum distance with the 8 points of the cube, poor method
        /*double min = Double.POSITIVE_INFINITY;
         min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(minYoctree-y)*(minYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
         min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(minYoctree-y)*(minYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
         min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
         min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
         min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(minYoctree-y)*(minYoctree-y)+(minZoctree-z)*(minZoctree-z)));
         min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(minYoctree-y)*(minYoctree-y)+(minZoctree-z)*(minZoctree-z)));
         min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(minZoctree-z)*(minZoctree-z)));
         min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(minZoctree-z)*(minZoctree-z)));
         return (float)min;*/
    }

    public void setClosestPoint(Vec3f closestPoint) {
        this.closestPoint = closestPoint;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(float minWeight) {
        this.minWeight = minWeight;
    }
}
