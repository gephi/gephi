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

import java.util.HashMap;
import java.util.Map;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterLibraryMask;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterLibraryImpl implements FilterLibrary {

    private AbstractLookup lookup;
    private InstanceContent content;
    private Map<Class<? extends Filter>, FilterBuilder> buildersMap;

    public FilterLibraryImpl() {
        content = new InstanceContent();
        lookup = new AbstractLookup(content);

        for (FilterBuilder builder : Lookup.getDefault().lookupAll(FilterBuilder.class)) {
            content.add(builder);
        }

        for (Query query : Lookup.getDefault().lookupAll(Query.class)) {
            content.add(query);
        }

        for (CategoryBuilder catBuilder : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            content.add(catBuilder);
        }

        content.add(new HierarchicalGraphMask());
    }

    private void buildBuildersMap() {
        buildersMap = new HashMap<Class<? extends Filter>, FilterBuilder>();
        for (FilterBuilder builder : lookup.lookupAll(FilterBuilder.class)) {
            try {
                Filter f = builder.getFilter();
                buildersMap.put(f.getClass(), builder);
                builder.destroy(f);
            } catch (Exception e) {
            }
        }
        for (CategoryBuilder catBuilder : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            for (FilterBuilder builder : catBuilder.getBuilders()) {
                try {
                    Filter f = builder.getFilter();
                    buildersMap.put(f.getClass(), builder);
                    builder.destroy(f);
                } catch (Exception e) {
                }
            }
        }
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void addBuilder(FilterBuilder builder) {
        content.add(builder);
    }

    public void removeBuilder(FilterBuilder builder) {
        content.remove(builder);
    }

    public void registerMask(FilterLibraryMask mask) {
        content.add(mask);
    }

    public void unregisterMask(FilterLibraryMask mask) {
        content.remove(mask);
    }

    public FilterBuilder getBuilder(Filter filter) {
        if (buildersMap == null) {
            buildBuildersMap();
        }
        if (buildersMap.get(filter.getClass()) != null) {
            return buildersMap.get(filter.getClass());
        }
        buildBuildersMap();
        if (buildersMap.get(filter.getClass()) != null) {
            return buildersMap.get(filter.getClass());
        }
        return null;
    }

    public void saveQuery(Query query) {
        content.add(query);
    }

    public void deleteQuery(Query query) {
        content.remove(query);
    }

    private static class HierarchicalGraphMask implements FilterLibraryMask {

        public Category getCategory() {
            return FilterLibrary.HIERARCHY;
        }

        public boolean isValid() {
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            return graphModel.isHierarchical();
        }
    }
}
