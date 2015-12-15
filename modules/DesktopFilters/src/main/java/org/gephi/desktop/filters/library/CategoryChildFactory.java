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
package org.gephi.desktop.filters.library;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class CategoryChildFactory extends ChildFactory<Object> {

    private Category category;
    private FiltersExplorer.Utils utils;

    public CategoryChildFactory(FiltersExplorer.Utils utils, Category category) {
        this.utils = utils;
        this.category = category;
    }

    @Override
    protected boolean createKeys(List<Object> toPopulate) {
        Object[] children = utils.getChildren(category);
        Arrays.sort(children, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                String s1;
                String s2;
                if (o1 == FiltersExplorer.QUERIES || o2 == FiltersExplorer.QUERIES) {
                    return o1 == FiltersExplorer.QUERIES ? 1 : -1;
                } else if (o1 instanceof Category && o2 instanceof Category) {
                    s1 = ((Category) o1).getName();
                    s2 = ((Category) o2).getName();
                    return s1.compareTo(s2);
                } else if (o1 instanceof FilterBuilder && o2 instanceof FilterBuilder) {
                    s1 = ((FilterBuilder) o1).getName();
                    s2 = ((FilterBuilder) o2).getName();
                    return s1.compareTo(s2);
                } else if (o1 instanceof Query && o2 instanceof Query) {
                    s1 = ((Query) o1).getName();
                    s2 = ((Query) o2).getName();
                    return s1.compareTo(s2);
                } else if (o1 instanceof Category) {
                    return -1;
                }
                return 1;
            }
        });
        toPopulate.addAll(Arrays.asList(children));
        return true;
    }

    @Override
    protected Node[] createNodesForKey(Object key) {
        if (key instanceof Category) {
            return new Node[]{new CategoryNode(utils, (Category) key)};
        } else if (key instanceof FilterBuilder) {
            return new Node[]{new FilterBuilderNode((FilterBuilder) key)};
        } else {
            return new Node[]{new SavedQueryNode((Query) key)};
        }
    }
}
