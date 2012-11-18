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
package org.gephi.filters.api;

import javax.swing.event.ChangeListener;
import org.gephi.filters.spi.FilterBuilder;

/**
 * The Filter Model hosts the queries defined in the system and the currently
 * active query. It also stroe the selection or filtering flag. The filtering mode
 * display the subgraph made from filters, whereas the selection mode highlight
 * elements on the graph.
 *
 * @author Mathieu Bastian
 * @see FilterController
 */
public interface FilterModel {

    /**
     * Returns the <code>FilterLibrary</code>, where {@link FilterBuilder}
     * belongs to.
     * @return          the filter library
     */
    public FilterLibrary getLibrary();

    /**
     * Returns all queries in the model, represented by their root query.
     * @return          all root queries in the model
     */
    public Query[] getQueries();

    /**
     * Returns the query currently active or <code>null</code> if none is active.
     * @return          the current query
     */
    public Query getCurrentQuery();

    /**
     * Returns <code>true</code> if the system is currently in filtering mode.
     * @return          <code>true</code> if the result graph is filtered,
     * <code>false</code> if it's in selection mode
     */
    public boolean isFiltering();

    /**
     * Returns <code>true</code> if the system is currently in selection mode.
     * @return          <code>true</code> if the result is selected on the graph,
     * <code>false</code> if it's filtered
     */
    public boolean isSelecting();

    public boolean isAutoRefresh();

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);
}
