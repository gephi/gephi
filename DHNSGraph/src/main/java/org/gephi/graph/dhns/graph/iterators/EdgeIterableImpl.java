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
package org.gephi.graph.dhns.graph.iterators;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.predicate.Predicate;

/**
 * Implementation of <code>EdgeIterable</code> with automatic lock unlocking when <code>hasNext</code>
 * returns <code>false</code>.
 * <p>
 * The <code>doBreak</code> method has to be called if the loop is interrupted.
 *
 * @author Mathieu Bastian
 */
public class EdgeIterableImpl implements EdgeIterable {

    private EdgeIteratorImpl iterator;

    public EdgeIterableImpl(AbstractEdgeIterator iterator, Lock lock) {
        this.iterator = new EdgeIteratorImpl(iterator, lock);
    }

    public EdgeIterableImpl(AbstractEdgeIterator iterator, Lock lock, Predicate<AbstractEdge> predicate) {
        this.iterator = new FilteredEdgeIteratorImpl(iterator, lock, predicate);
    }

    public EdgeIterableImpl(EdgeIterableImpl iterable, Predicate<AbstractEdge> predicate) {
        this(iterable.getIterator().getIterator(), iterable.getIterator().getLock(), predicate);
    }

    public EdgeIterator iterator() {
        return iterator;
    }

    public void doBreak() {
        if (iterator.lock != null) {
            iterator.lock.unlock();
        }
    }

    public Edge[] toArray() {
        ArrayList<Edge> list = new ArrayList<Edge>();
        for (; iterator.hasNext();) {
            list.add(iterator.next());
        }
        return list.toArray(new Edge[0]);
    }

    public EdgeIteratorImpl getIterator() {
        return iterator;
    }
}
