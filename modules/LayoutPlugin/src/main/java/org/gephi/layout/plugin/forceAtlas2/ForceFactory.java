/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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

package org.gephi.layout.plugin.forceAtlas2;

import org.gephi.graph.api.Node;

/**
 * Generates the forces on demand, here are all the formulas for attraction and
 * repulsion.
 *
 * @author Mathieu Jacomy
 */
public class ForceFactory {

    public static ForceFactory builder = new ForceFactory();

    private ForceFactory() {
    }

    public AttractionForce buildAttraction(ForceAtlas2.ForceAtlas2Params params) {
        if (params.adjustSizes) {
            if (params.linLogMode) {
                if (params.outboundAttractionDistribution) {
                    return new logAttraction_degreeDistributed_antiCollision(params);
                } else {
                    return new logAttraction_antiCollision(params);
                }
            } else {
                if (params.outboundAttractionDistribution) {
                    return new linAttraction_degreeDistributed_antiCollision(params);
                } else {
                    return new linAttraction_antiCollision(params);
                }
            }
        } else {
            if (params.linLogMode) {
                if (params.outboundAttractionDistribution) {
                    return new logAttraction_degreeDistributed(params);
                } else {
                    return new logAttraction(params);
                }
            } else {
                if (params.outboundAttractionDistribution) {
                    return new linAttraction_massDistributed(params);
                } else {
                    return new linAttraction(params);
                }
            }
        }
    }

    public abstract class AttractionForce {

        public abstract void apply(Node n1, Node n2,
                                   double e); // Model for node-node attraction (e is for edge weight if needed)
    }



    /*
     * Attraction force: Linear
     */
    private class linAttraction extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public linAttraction(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();

            // NB: factor = force / distance
            double factor = -params.outboundAttCompensation * e;

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }

    /*
     * Attraction force: Linear, distributed by mass (typically, degree)
     */
    private class linAttraction_massDistributed extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public linAttraction_massDistributed(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();

            // NB: factor = force / distance
            double factor = -params.outboundAttCompensation * e / n1Layout.mass;

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }

    /*
     * Attraction force: Logarithmic
     */
    private class logAttraction extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public logAttraction(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -params.outboundAttCompensation * e * Math.log(1 + distance) / distance;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, distributed by Degree
     */
    private class logAttraction_degreeDistributed extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public logAttraction_degreeDistributed(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -params.outboundAttCompensation * e * Math.log(1 + distance) / distance / n1Layout.mass;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, with Anti-Collision
     */
    private class linAttraction_antiCollision extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public linAttraction_antiCollision(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1.size() - n2.size();

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = -this.params.outboundAttCompensation * e;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, distributed by Degree, with Anti-Collision
     */
    private class linAttraction_degreeDistributed_antiCollision extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public linAttraction_degreeDistributed_antiCollision(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1.size() - n2.size();

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = -this.params.outboundAttCompensation * e / n1Layout.mass;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Logarithmic, with Anti-Collision
     */
    private class logAttraction_antiCollision extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public logAttraction_antiCollision(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1.size() - n2.size();

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -params.outboundAttCompensation * e * Math.log(1 + distance) / distance;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, distributed by Degree, with Anti-Collision
     */
    private class logAttraction_degreeDistributed_antiCollision extends AttractionForce {

        private final ForceAtlas2.ForceAtlas2Params params;

        public logAttraction_degreeDistributed_antiCollision(ForceAtlas2.ForceAtlas2Params params) {
            this.params = params;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1.size() - n2.size();

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -this.params.outboundAttCompensation* e * Math.log(1 + distance) / distance / n1Layout.mass;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }
}
