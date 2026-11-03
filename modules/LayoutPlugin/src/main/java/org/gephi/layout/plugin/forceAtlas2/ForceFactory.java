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
public final class ForceFactory {

    static final ForceFactory INSTANCE = new ForceFactory();

    private ForceFactory() {
    }

    public RepulsionForce buildRepulsion(boolean adjustBySize, double coefficient) {
        if (adjustBySize) {
            return new LinearRepulsionAntiCollision(coefficient);
        } else {
            return new LinearRepulsion(coefficient);
        }
    }

    public RepulsionForce getStrongGravity(double coefficient) {
        return new StrongGravity(coefficient);
    }

    public AttractionForce buildAttraction(boolean logAttraction, boolean distributedAttraction, boolean adjustBySize,
                                           double coefficient) {
        if (adjustBySize) {
            if (logAttraction) {
                if (distributedAttraction) {
                    return new LogAttractionDegreeDistributedAntiCollision(coefficient);
                } else {
                    return new LogAttractionAntiCollision(coefficient);
                }
            } else {
                if (distributedAttraction) {
                    return new LinearAttractionDegreeDistributedAntiCollision(coefficient);
                } else {
                    return new LinearAttractionAntiCollision(coefficient);
                }
            }
        } else {
            if (logAttraction) {
                if (distributedAttraction) {
                    return new LogAttractionDegreeDistributed(coefficient);
                } else {
                    return new LogAttraction(coefficient);
                }
            } else {
                if (distributedAttraction) {
                    return new LinearAttractionMassDistributed(coefficient);
                } else {
                    return new LinearAttraction(coefficient);
                }
            }
        }
    }

    public static abstract class AttractionForce {

        public abstract void apply(Node n1, Node n2,
                                   double e); // Model for node-node attraction (e is for edge weight if needed)
    }

    public static abstract class RepulsionForce {

        public abstract void apply(Node n1, Node n2);           // Model for node-node repulsion

        public abstract void apply(Node n, Region r);           // Model for Barnes Hut approximation

        public abstract void apply(Node n, double g);           // Model for gravitation (anti-repulsion)
    }

    /*
     * Repulsion force: Linear
     */
    private static class LinearRepulsion extends RepulsionForce {

        private final double coefficient;

        private LinearRepulsion(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * n1Layout.mass * n2Layout.mass / distance / distance;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }

        @Override
        public void apply(Node n, Region r) {
            ForceAtlas2LayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x() - r.getMassCenterX();
            double yDist = n.y() - r.getMassCenterY();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * r.getMass() / distance / distance;

                nLayout.dx += xDist * factor;
                nLayout.dy += yDist * factor;
            }
        }

        @Override
        public void apply(Node n, double g) {
            ForceAtlas2LayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x();
            double yDist = n.y();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * g / distance;

                nLayout.dx -= xDist * factor;
                nLayout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Repulsion force: Strong Gravity (as a Repulsion Force because it is easier)
     */
    private static class LinearRepulsionAntiCollision extends RepulsionForce {

        private final double coefficient;

        private LinearRepulsionAntiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist) - n1.size() - n2.size();

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * n1Layout.mass * n2Layout.mass / distance / distance;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;

            } else if (distance < 0) {
                double factor = 100 * coefficient * n1Layout.mass * n2Layout.mass;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }

        @Override
        public void apply(Node n, Region r) {
            ForceAtlas2LayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x() - r.getMassCenterX();
            double yDist = n.y() - r.getMassCenterY();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * r.getMass() / distance / distance;

                nLayout.dx += xDist * factor;
                nLayout.dy += yDist * factor;
            } else if (distance < 0) {
                double factor = -coefficient * nLayout.mass * r.getMass() / distance;

                nLayout.dx += xDist * factor;
                nLayout.dy += yDist * factor;
            }
        }

        @Override
        public void apply(Node n, double g) {
            ForceAtlas2LayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x();
            double yDist = n.y();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * g / distance;

                nLayout.dx -= xDist * factor;
                nLayout.dy -= yDist * factor;
            }
        }
    }

    private static class StrongGravity extends RepulsionForce {

        private final double coefficient;

        private StrongGravity(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2) {
            // Not Relevant
        }

        @Override
        public void apply(Node n, Region r) {
            // Not Relevant
        }

        @Override
        public void apply(Node n, double g) {
            ForceAtlas2LayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x();
            double yDist = n.y();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * g;

                nLayout.dx -= xDist * factor;
                nLayout.dy -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear
     */
    private static class LinearAttraction extends AttractionForce {

        private final double coefficient;

        private LinearAttraction(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();

            // NB: factor = force / distance
            double factor = -coefficient * e;

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }

    /*
     * Attraction force: Linear, distributed by mass (typically, degree)
     */
    private static class LinearAttractionMassDistributed extends AttractionForce {

        private final double coefficient;

        private LinearAttractionMassDistributed(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();

            // NB: factor = force / distance
            double factor = -coefficient * e / n1Layout.mass;

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }

    /*
     * Attraction force: Logarithmic
     */
    private static class LogAttraction extends AttractionForce {

        private final double coefficient;

        private LogAttraction(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance;

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
    private static class LogAttractionDegreeDistributed extends AttractionForce {

        private final double coefficient;

        private LogAttractionDegreeDistributed(double c) {
            coefficient = c;
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
                double factor = -coefficient * e * Math.log(1 + distance) / distance / n1Layout.mass;

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
    private static class LinearAttractionAntiCollision extends AttractionForce {

        private final double coefficient;

        private LinearAttractionAntiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist) - n1.size() - n2.size();

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = -coefficient * e;

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
    private static class LinearAttractionDegreeDistributedAntiCollision extends AttractionForce {

        private final double coefficient;

        private LinearAttractionDegreeDistributedAntiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist) - n1.size() - n2.size();

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = -coefficient * e / n1Layout.mass;

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
    private static class LogAttractionAntiCollision extends AttractionForce {

        private final double coefficient;

        private LogAttractionAntiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist) - n1.size() - n2.size();

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance;

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
    private static class LogAttractionDegreeDistributedAntiCollision extends AttractionForce {

        private final double coefficient;

        private LogAttractionDegreeDistributedAntiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
            ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.hypot(xDist, yDist) - n1.size() - n2.size();

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance / n1Layout.mass;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }
}
