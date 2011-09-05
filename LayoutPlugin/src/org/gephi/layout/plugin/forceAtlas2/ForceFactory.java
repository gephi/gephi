/*
Copyright 2008-2011 Gephi
Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.layout.plugin.forceAtlas2;

import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;

/**
 * Generates the forces on demand, here are all the formulas for attraction and repulsion.
 * @author Mathieu Jacomy
 */
public class ForceFactory {

    public static ForceFactory builder = new ForceFactory();

    private ForceFactory() {
    }

    ;

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

    public AttractionForce buildAttraction(boolean logAttraction, boolean distributedAttraction, boolean adjustBySize, double coefficient) {
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

        public abstract void apply(Node n1, Node n2, double e); // Model for node-node attraction (e is for edge weight if needed)
    }

    public abstract class RepulsionForce {

        public abstract void apply(Node n1, Node n2);           // Model for node-node repulsion

        public abstract void apply(Node n, Region r);           // Model for Barnes Hut approximation

        public abstract void apply(Node n, double g);           // Model for gravitation (anti-repulsion)
    }

    /*
     * Repulsion force: Linear
     */
    private class linRepulsion extends RepulsionForce {

        private double coefficient;

        public linRepulsion(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

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
            NodeData nData = n.getNodeData();
            ForceAtlas2LayoutData nLayout = nData.getLayoutData();

            // Get the distance
            double xDist = nData.x() - r.getMassCenterX();
            double yDist = nData.y() - r.getMassCenterY();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * r.getMass() / distance / distance;

                nLayout.dx += xDist * factor;
                nLayout.dy += yDist * factor;
            }
        }

        @Override
        public void apply(Node n, double g) {
            NodeData nData = n.getNodeData();
            ForceAtlas2LayoutData nLayout = nData.getLayoutData();

            // Get the distance
            double xDist = nData.x();
            double yDist = nData.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

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
    private class linRepulsion_antiCollision extends RepulsionForce {

        private double coefficient;

        public linRepulsion_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1Data.getSize() - n2Data.getSize();

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
            NodeData nData = n.getNodeData();
            ForceAtlas2LayoutData nLayout = nData.getLayoutData();

            // Get the distance
            double xDist = nData.x() - r.getMassCenterX();
            double yDist = nData.y() - r.getMassCenterY();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

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
            NodeData nData = n.getNodeData();
            ForceAtlas2LayoutData nLayout = nData.getLayoutData();

            // Get the distance
            double xDist = nData.x();
            double yDist = nData.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * g / distance;

                nLayout.dx -= xDist * factor;
                nLayout.dy -= yDist * factor;
            }
        }
    }

    private class strongGravity extends RepulsionForce {

        private double coefficient;

        public strongGravity(double c) {
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
            NodeData nData = n.getNodeData();
            ForceAtlas2LayoutData nLayout = nData.getLayoutData();

            // Get the distance
            double xDist = nData.x();
            double yDist = nData.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

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
    private class linAttraction extends AttractionForce {

        private double coefficient;

        public linAttraction(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();

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
    private class linAttraction_massDistributed extends AttractionForce {

        private double coefficient;

        public linAttraction_massDistributed(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();

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
    private class logAttraction extends AttractionForce {

        private double coefficient;

        public logAttraction(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

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
    private class logAttraction_degreeDistributed extends AttractionForce {

        private double coefficient;

        public logAttraction_degreeDistributed(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
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
    private class linAttraction_antiCollision extends AttractionForce {

        private double coefficient;

        public linAttraction_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1Data.getSize() - n2Data.getSize();

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
    private class linAttraction_degreeDistributed_antiCollision extends AttractionForce {

        private double coefficient;

        public linAttraction_degreeDistributed_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1Data.getSize() - n2Data.getSize();

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
    private class logAttraction_antiCollision extends AttractionForce {

        private double coefficient;

        public logAttraction_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1Data.getSize() - n2Data.getSize();

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
    private class logAttraction_degreeDistributed_antiCollision extends AttractionForce {

        private double coefficient;

        public logAttraction_degreeDistributed_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            NodeData n1Data = n1.getNodeData();
            ForceAtlas2LayoutData n1Layout = n1Data.getLayoutData();
            NodeData n2Data = n2.getNodeData();
            ForceAtlas2LayoutData n2Layout = n2Data.getLayoutData();

            // Get the distance
            double xDist = n1Data.x() - n2Data.x();
            double yDist = n1Data.y() - n2Data.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1Data.getSize() - n2Data.getSize();

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
