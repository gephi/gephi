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

package org.gephi.graph.dhns.filter;

import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.proposition.Tautology;
import org.gephi.graph.dhns.utils.avl.AbstractEdgeTree;
import org.gephi.graph.dhns.utils.avl.AbstractNodeTree;

/**
 *  Cette classe contient un etant du graphe, après filtrage. Un graphe non filtré na pas besoin de cache car les iterateurs directs
 * sont assez rapides pour fonctionner en boucle continue. Cette cache doit être regénéré à la demande de lecture du graphe après une
 * modification. Ces modifications peuvent être structurelles (ajou/suppression de noeuds, changement dans la hierarchie) ou bien
 * liées aux filtres eux memes. En effet lorsqu'un paramètre de filtres est modifiée, le filtrage change et donc le graphe résultat change.
 * Ceci doit être très rapide afin de permettre les changements reguliers, mais doit aussi faire profiter une reduction du nombre d'elements
 * a iterer.
 * On doit pouvoir paramétrer la mise à jour automatique ou non de cette cache.
 * Cette cache est propre à une instance de graphe, et pourra eventuellement s'echanger/se cloner.
 * Cette chache représente ce qui subsiste après filtrage. Elle est utilisée par les methodes annexes comme getNeigbors(Node). En effet
 * la methode getNeighbors se base sur le graphe complet et lance son iterateur sur les edges de Node. C'est grace au test evaluate(edge) et
 * evaluate(Node) que l'iterateurs de getNieighbors donne le bon resultat.
 * @author Mathieu
 */
public class FilterControl {

    private FilterResult currentResult;
    private Dhns dhns;

    public FilterControl(Dhns dhns) {
        this.dhns = dhns;
        currentResult = new FilterResult(new AbstractNodeTree(), new AbstractEdgeTree());
    }

    public FilterResult getCurrentFilterResult() {
        return currentResult;
    }

    private boolean update() {
        AbstractNodeTree nodeTree = new AbstractNodeTree();
        TreeIterator nodeIterator = new TreeIterator(dhns.getTreeStructure(), new Tautology());
        for(;nodeIterator.hasNext();) {
            nodeTree.add(nodeIterator.next());
        }
        AbstractEdgeTree edgeTree = new AbstractEdgeTree();

        FilterResult filterResult = new FilterResult(nodeTree, edgeTree);
        currentResult = filterResult;
        
        return true;
    }

    public void filterParameterUpdated() {
        dhns.getWriteLock().lock();
        update();
        dhns.getWriteLock().unlock();
    }
}
