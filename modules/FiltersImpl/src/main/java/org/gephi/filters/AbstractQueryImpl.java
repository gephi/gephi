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
package org.gephi.filters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Graph;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractQueryImpl implements Query {

    protected List<AbstractQueryImpl> children;
    protected Query parent;
    protected Graph result;

    public AbstractQueryImpl() {
        this.children = new ArrayList<AbstractQueryImpl>();
    }

    public abstract int getChildrenSlotsCount();

    public abstract int getPropertiesCount();

    public abstract String getPropertyName(int index);

    public abstract Object getPropertyValue(int index);

    public abstract String getName();

    public abstract void setName(String name);

    public Query[] getChildren() {
        return children.toArray(new Query[0]);
    }

    public int getChildrenCount() {
        return children.size();
    }

    public AbstractQueryImpl getChildAt(int index) {
        return children.get(index);
    }

    public void addSubQuery(Query subQuery) {
        children.add((AbstractQueryImpl) subQuery);
        ((AbstractQueryImpl) subQuery).setParent(this);
    }

    public void removeSubQuery(Query subQuery) {
        children.remove((AbstractQueryImpl) subQuery);
    }

    public Query getParent() {
        return parent;
    }

    public void setParent(Query parent) {
        this.parent = parent;
    }

    public void setResult(Graph result) {
        this.result = result;
    }

    public Graph getResult() {
        return result;
    }

    public AbstractQueryImpl getRoot() {
        AbstractQueryImpl root = this;
        while (root.getParent() != null) {
            root = (AbstractQueryImpl) root.getParent();
        }
        return root;
    }

    public AbstractQueryImpl[] getLeaves() {
        ArrayList<AbstractQueryImpl> leaves = new ArrayList<AbstractQueryImpl>();
        Deque<Query> stack = new ArrayDeque<Query>();
        stack.add(this);
        while (!stack.isEmpty()) {
            AbstractQueryImpl query = (AbstractQueryImpl) stack.pop();
            if (query.children.size() > 0) {
                stack.addAll(query.children);
            } else {
                //Leaf
                leaves.add(query);
            }
        }
        return leaves.toArray(new AbstractQueryImpl[0]);
    }

    public AbstractQueryImpl copy() {
        AbstractQueryImpl copy = null;
        if (this instanceof FilterQueryImpl) {
            copy = new FilterQueryImpl(this.getFilter());
        } else if (this instanceof OperatorQueryImpl) {
            copy = new OperatorQueryImpl((Operator) this.getFilter());
        }

        for (int i = 0; i < children.size(); i++) {
            AbstractQueryImpl child = (AbstractQueryImpl) children.get(i);
            AbstractQueryImpl childCopy = child.copy();
            childCopy.parent = copy;
            copy.children.add(childCopy);
        }

        return copy;
    }

    public Query[] getQueries(Class<? extends Filter> filterClass) {
        List<Query> r = new LinkedList<Query>();
        LinkedList<Query> stack = new LinkedList<Query>();
        stack.add(this);
        while (!stack.isEmpty()) {
            Query q = stack.pop();
            r.add(q);
            stack.addAll(Arrays.asList(q.getChildren()));
        }
        for (Iterator<Query> itr = r.iterator(); itr.hasNext();) {
            Query q = itr.next();
            if (!q.getFilter().getClass().equals(filterClass)) {
                itr.remove();
            }
        }
        return r.toArray(new Query[0]);
    }

    public Query[] getDescendantsAndSelf() {
        List<Query> r = new LinkedList<Query>();
        LinkedList<Query> stack = new LinkedList<Query>();
        stack.add(this);
        while (!stack.isEmpty()) {
            Query q = stack.pop();
            r.add(q);
            stack.addAll(Arrays.asList(q.getChildren()));
        }
        return r.toArray(new Query[0]);
    }
}
