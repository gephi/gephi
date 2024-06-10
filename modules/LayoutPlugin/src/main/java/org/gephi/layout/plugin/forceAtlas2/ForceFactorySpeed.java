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
public class ForceFactorySpeed {

    public static ForceFactorySpeed builder = new ForceFactorySpeed();

    private ForceFactorySpeed() {
    }

    public RepulsionForce buildRepulsion(boolean adjustBySize, double coefficient) {
        if (adjustBySize) {
            return new linRepulsion_antiCollision(coefficient);
        } else {
            return new linRepulsion(coefficient);
        }
    }

    public RepulsionForce getStrongGravity(double coefficient) {
        return new strongGravity(coefficient);
    }

    public AttractionForce buildAttraction(boolean logAttraction, boolean distributedAttraction, boolean adjustBySize,
            double coefficient) {
        if (adjustBySize) {
            if (logAttraction) {
                if (distributedAttraction) {
                    return new logAttraction_degreeDistributed_antiCollision(coefficient);
                } else {
                    return new logAttraction_antiCollision(coefficient);
                }
            } else {
                if (distributedAttraction) {
                    return new linAttraction_degreeDistributed_antiCollision(coefficient);
                } else {
                    return new linAttraction_antiCollision(coefficient);
                }
            }
        } else {
            if (logAttraction) {
                if (distributedAttraction) {
                    return new logAttraction_degreeDistributed(coefficient);
                } else {
                    return new logAttraction(coefficient);
                }
            } else {
                if (distributedAttraction) {
                    return new linAttraction_massDistributed(coefficient);
                } else {
                    return new linAttraction(coefficient);
                }
            }
        }
    }

    public abstract class AttractionForce {

        public abstract void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e); // Model for node-node attraction (e is for edge weight if needed)
    }

    public abstract class RepulsionForce {

        public abstract void applyClassicRepulsion(double[] nodesInfo, int indexNode1, int indexNode2);           // Model for node-node repulsion

        public abstract void applyBarnesHutRepulsion(double[] nodesInfo, int nodeIndex, RegionSpeed r);           // Model for Barnes Hut approximation

        public abstract void applyGravity(double[] nodesInfo, int indexNode, double g);           // Model for gravitation (anti-repulsion)
    }

    /*
     * Repulsion force: Linear
     */
    private class linRepulsion extends RepulsionForce {

        private final double coefficient;

        public linRepulsion(double c) {
            coefficient = c;
        }

        @Override
        public void applyClassicRepulsion(double[] nodesInfo, int indexNode1, int indexNode2) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode1 + 2];
            double yDist = nodesInfo[indexNode2 + 2] - nodesInfo[indexNode2 + 2];
            double distSquared = xDist * xDist + yDist * yDist;
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nodesInfo[indexNode1 + 1] * nodesInfo[indexNode2 + 1] / distance / distance;

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }

        @Override
        public void applyBarnesHutRepulsion(double[] nodesInfo, int nodeIndex, RegionSpeed r) {

            // Get the distance
            double xDist = nodesInfo[nodeIndex + 2] - r.getMassCenterX();
            double yDist = nodesInfo[nodeIndex + 3] - r.getMassCenterY();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nodesInfo[nodeIndex + 1] * r.getMass() / distance / distance;

                nodesInfo[nodeIndex + 6] += xDist * factor;
                nodesInfo[nodeIndex + 7] += yDist * factor;
            }
        }

        @Override
        public void applyGravity(double[] nodesInfo, int indexNode, double g) {

            // Get the distance
            double xDist = nodesInfo[indexNode + 2];
            double yDist = nodesInfo[indexNode + 3];
            double distSquared = xDist * xDist + yDist * yDist;
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nodesInfo[indexNode + 1] * g / distance;

                nodesInfo[indexNode + 6] -= xDist * factor;
                nodesInfo[indexNode + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Repulsion force: Strong Gravity (as a Repulsion Force because it is easier)
     */
    private class linRepulsion_antiCollision extends RepulsionForce {

        private final double coefficient;

        public linRepulsion_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void applyClassicRepulsion(double[] nodesInfo, int indexNode1, int indexNode2) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode1 + 2];
            double yDist = nodesInfo[indexNode2 + 2] - nodesInfo[indexNode2 + 2];
            double distSquared = xDist * xDist + yDist * yDist - nodesInfo[indexNode1 + 8] - nodesInfo[indexNode2 + 8];
            double distance = 1.0 / fastInverseSqrt(distSquared);

            double factor = 1;
            if (distance > 0) {
                // NB: factor = force / distance
                factor = coefficient * nodesInfo[indexNode1 + 1] * nodesInfo[indexNode2 + 1] / distance / distance;
            } else if (distance < 0) {
                factor = 100 * coefficient * nodesInfo[indexNode1 + 1] * nodesInfo[indexNode2 + 1];
            }
            nodesInfo[indexNode1 + 6] += xDist * factor;
            nodesInfo[indexNode1 + 7] += yDist * factor;

            nodesInfo[indexNode2 + 6] -= xDist * factor;
            nodesInfo[indexNode2 + 7] -= yDist * factor;
        }

        @Override
        public void applyBarnesHutRepulsion(double[] nodesInfo, int nodeIndex, RegionSpeed r) {

            // Get the distance
            double xDist = nodesInfo[nodeIndex + 2] - r.getMassCenterX();
            double yDist = nodesInfo[nodeIndex + 3] - r.getMassCenterY();
            double distSquared = xDist * xDist + yDist * yDist;
            double distance = 1.0 / fastInverseSqrt(distSquared);

            double factor = 1;
            if (distance > 0) {
                // NB: factor = force / distance
                factor = coefficient * nodesInfo[nodeIndex + 1] * r.getMass() / distance / distance;
            } else if (distance < 0) {
                factor = -coefficient * nodesInfo[nodeIndex + 1] * r.getMass() / distance;
            }
            nodesInfo[nodeIndex + 6] += xDist * factor;
            nodesInfo[nodeIndex + 7] += yDist * factor;
        }

        @Override
        public void applyGravity(double[] nodesInfo, int indexNode, double g) {

            // Get the distance
            double xDist = nodesInfo[indexNode + 2];
            double yDist = nodesInfo[indexNode + 3];
            double distSquared = xDist * xDist + yDist * yDist;
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nodesInfo[indexNode + 1] * g / distance;

                nodesInfo[indexNode + 6] -= xDist * factor;
                nodesInfo[indexNode + 7] -= yDist * factor;
            }
        }
    }

    private class strongGravity extends RepulsionForce {

        private final double coefficient;

        public strongGravity(double c) {
            coefficient = c;
        }

        @Override
        public void applyClassicRepulsion(double[] nodesInfo, int indexNode1, int indexNode2) {
            // Not Relevant
        }

        @Override
        public void applyBarnesHutRepulsion(double[] nodesInfo, int nodeIndex, RegionSpeed r) {
            // Not Relevant
        }

        @Override
        public void applyGravity(double[] nodesInfo, int indexNode, double g) {
            // Get the distance
            double xDist = nodesInfo[indexNode + 2];
            double yDist = nodesInfo[indexNode + 3];
            double distSquared = xDist * xDist + yDist * yDist;
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {
                double factor = coefficient * nodesInfo[indexNode + 1] * g;

                nodesInfo[indexNode + 6] -= xDist * factor;
                nodesInfo[indexNode + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear
     */
    private class linAttraction extends AttractionForce {

        private final double coefficient;

        public linAttraction(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];

            // NB: factor = force / distance
            double factor = -coefficient * e;

            nodesInfo[indexNode1 + 6] += xDist * factor;
            nodesInfo[indexNode1 + 7] += yDist * factor;

            nodesInfo[indexNode2 + 6] -= xDist * factor;
            nodesInfo[indexNode2 + 7] -= yDist * factor;
        }
    }

    /*
     * Attraction force: Linear, distributed by mass (typically, degree)
     */
    private class linAttraction_massDistributed extends AttractionForce {

        private final double coefficient;

        public linAttraction_massDistributed(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];

            // NB: factor = force / distance
            double factor = -coefficient * e / nodesInfo[indexNode1 + 1];

            nodesInfo[indexNode1 + 6] += xDist * factor;
            nodesInfo[indexNode1 + 7] += yDist * factor;

            nodesInfo[indexNode2 + 6] -= xDist * factor;
            nodesInfo[indexNode2 + 7] -= yDist * factor;
        }
    }

    /*
     * Attraction force: Logarithmic
     */
    private class logAttraction extends AttractionForce {

        private final double coefficient;

        public logAttraction(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];
            double distance = Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance;

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, distributed by Degree
     */
    private class logAttraction_degreeDistributed extends AttractionForce {

        private final double coefficient;

        public logAttraction_degreeDistributed(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];
            double distSquared = xDist * xDist + yDist * yDist;
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance / nodesInfo[indexNode1 + 1];

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, with Anti-Collision
     */
    private class linAttraction_antiCollision extends AttractionForce {

        private final double coefficient;

        public linAttraction_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];
            double distSquared = xDist * xDist + yDist * yDist - nodesInfo[indexNode1 + 9] - nodesInfo[indexNode2 + 9];
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = -coefficient * e;

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, distributed by Degree, with Anti-Collision
     */
    private class linAttraction_degreeDistributed_antiCollision extends AttractionForce {

        private final double coefficient;

        public linAttraction_degreeDistributed_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];
            double distSquared = xDist * xDist + yDist * yDist - nodesInfo[indexNode1 + 9] - nodesInfo[indexNode2 + 9];
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = -coefficient * e / nodesInfo[indexNode1 + 1];

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Logarithmic, with Anti-Collision
     */
    private class logAttraction_antiCollision extends AttractionForce {

        private final double coefficient;

        public logAttraction_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];
            double distSquared = xDist * xDist + yDist * yDist - nodesInfo[indexNode1 + 9] - nodesInfo[indexNode2 + 9];
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance;

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }
    }

    /*
     * Attraction force: Linear, distributed by Degree, with Anti-Collision
     */
    private class logAttraction_degreeDistributed_antiCollision extends AttractionForce {

        private final double coefficient;

        public logAttraction_degreeDistributed_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(double[] nodesInfo, int indexNode1, int indexNode2, double e) {

            // Get the distance
            double xDist = nodesInfo[indexNode1 + 2] - nodesInfo[indexNode2 + 2];
            double yDist = nodesInfo[indexNode1 + 3] - nodesInfo[indexNode2 + 3];
            double distSquared = xDist * xDist + yDist * yDist - nodesInfo[indexNode1 + 9] - nodesInfo[indexNode2 + 9];
            double distance = 1.0 / fastInverseSqrt(distSquared);

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(1 + distance) / distance / nodesInfo[indexNode1 + 1];

                nodesInfo[indexNode1 + 6] += xDist * factor;
                nodesInfo[indexNode1 + 7] += yDist * factor;

                nodesInfo[indexNode2 + 6] -= xDist * factor;
                nodesInfo[indexNode2 + 7] -= yDist * factor;
            }
        }
    }

    public static double fastInverseSqrt(double x) {
        double xhalf = 0.5d * x;
        long i = Double.doubleToLongBits(x); // evil floating point bit level hacking
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x = x * (1.5d - xhalf * x * x); // 1st iteration
        // x = x * (1.5d - xhalf * x * x); // 2nd iteration can be added for more precision
        return x;
    }
}
