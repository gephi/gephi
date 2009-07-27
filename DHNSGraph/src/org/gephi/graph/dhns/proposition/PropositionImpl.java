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
package org.gephi.graph.dhns.proposition;

import java.util.Arrays;
import org.gephi.graph.api.Predicate;

/**
 *
 * @author Mathieu Bastian
 */
public class PropositionImpl<T> implements Proposition<T> {

    private Predicate<T>[] predicates;
    private boolean skipping = false; ///True when **enabled** predicate

    public PropositionImpl() {
        this.predicates = new Predicate[0];
    }

    public PropositionImpl(Predicate<T>[] predicates) {
        this.predicates = predicates;
    }

    public boolean evaluate(T element) {
        for (Predicate p : predicates) {
            if (!p.evaluate(element)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTautology() {
        for (Predicate p : predicates) {
            if (!(p instanceof Tautology)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSkipping() {
        return skipping;
    }

    public void setSkipping(boolean skipping) {
        this.skipping = skipping;
    }

    public void addPredicate(Predicate<? super T> predicate) {
        predicates = Arrays.copyOf(predicates, predicates.length + 1);
        predicates[predicates.length - 1] = (Predicate<T>) predicate;
    }

    public void removePredicate(Predicate<? super T> predicate) {
        Predicate<T>[] newPredicates = new Predicate[predicates.length - 1];
        int j = 0, i = 0;
        for (; i < predicates.length; i++) {
            if (predicates[i] != predicate) {
                newPredicates[j] = predicates[i];
                j++;
            }
        }
        if (j != i) {
            predicates = newPredicates;
        }
    }

    public PropositionImpl<T> copy() {
        PropositionImpl<T> copy = new PropositionImpl<T>(Arrays.copyOf(predicates, predicates.length));
        copy.skipping = this.skipping;
        return copy;
    }
}
