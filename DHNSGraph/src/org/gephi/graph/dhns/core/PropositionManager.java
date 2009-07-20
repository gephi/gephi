/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.dhns.core;

import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.proposition.Predicate;
import org.gephi.graph.dhns.view.View;

/**
 *
 * @author Mathieu Bastian
 */
public class PropositionManager {

    private Dhns dhns;

    //Default
    private Predicate<AbstractNode> visiblePredicateNode;
    private Predicate<AbstractEdge> visiblePredicateEdge;

    public PropositionManager(Dhns dhns) {
        this.dhns = dhns;
        initDefaultNodePredicates();
        initDefaultEdgePredicates();
    }

    public Predicate<AbstractNode> getVisiblePredicateNode() {
        return visiblePredicateNode;
    }

    public Predicate<AbstractEdge> getVisiblePredicateEdge() {
        return visiblePredicateEdge;
    }

    public Predicate<AbstractNode> newEnablePredicateNode(final View view) {
        return new Predicate<AbstractNode>() {

            public boolean evaluate(AbstractNode element) {
                return element.isEnabled(view);
            }

            public boolean isTautology() {
                return false;
            }
        };
    }

    private void initDefaultNodePredicates() {
        visiblePredicateNode = new Predicate<AbstractNode>() {

            public boolean evaluate(AbstractNode element) {
                return element.isVisible();
            }

            public boolean isTautology() {
                return false;
            }
        };
    }

    private void initDefaultEdgePredicates() {
        visiblePredicateEdge = new Predicate<AbstractEdge>() {

            public boolean evaluate(AbstractEdge element) {
                return element.isVisible();
            }

            public boolean isTautology() {
                return false;
            }
        };
    }
}
