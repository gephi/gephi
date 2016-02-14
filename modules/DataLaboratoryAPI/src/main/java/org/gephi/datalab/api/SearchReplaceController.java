/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.api;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 * <p>Independent controller for search/replace feature.</p>
 * <p>Operates with <code>SearchOptions</code> and <code>SearchResult</code> objects.</p>
 * @author Eduardo Ramos
 */
public interface SearchReplaceController {

    /**
     * <p>Finds next (or first) ocurrence for the given search options.</p>
     * <p>Returns a <code>SearchResult</code> instance with the details or null if the search was not successful.</p>
     * <p>Modifies the given search options in order to match the next result the next time <code>findNext</code> is called</p>
     * @param searchOptions Options of the search
     * @return SearchResult with details of the match or null
     */
    SearchResult findNext(SearchOptions searchOptions);

    /**
     * <p>Finds next ocurrence for the given search options contained in a SearchResult.</p>
     * <p>Returns a <code>SearchResult</code> instance with the details or null if the search was not successful.</p>
     * <p>Modifies the given search options in order to match the next result the next time <code>findNext</code> is called</p>
     * @param result Last result of the search
     * @return SearchResult with details of the match or null 
     */
    SearchResult findNext(SearchResult result);

    /**
     * <p>Indicates if a <code>SearchResult</code> can be replaced or not.</p>
     * <p>Computed columns and id columns cannot be replaced.</p>
     * @param result SearchResult to check before replacing
     * @return True if it can be replaced, false otherwise
     */
    boolean canReplace(SearchResult result);

    /**
     * <p>Replaces a <code>SearchResult</code> with the given replacement String.</p>
     * <p>Also tries to find next search result and returns it.</p>
     * <p>If the data has changed and the replacement can't be done it will just return next <code>SearchResult</code> calling <code>findNext</code>.</p>
     * <p>If useRegexReplaceMode is enabled, IndexOutOfBoundsException can be thrown when the replacement is not correct for the regular expression.</p>
     * @param result SearchResult to replace
     * @param replacement Replacement String
     * @return Next SearchResult or null if not successful
     */
    SearchResult replace(SearchResult result, String replacement);

    /**
     * <p>Replaces all SearchResults that can be replaced with the given search options from the beginning to the end of the data.</p>
     * <p>If useRegexReplaceMode is enabled, IndexOutOfBoundsException can be thrown when the replacement is not correct for the regular expression.</p>
     * @param searchOptions Search options for the searches
     * @param replacement Replacement String
     * @return Count of made replacements
     */
    int replaceAll(SearchOptions searchOptions, String replacement);

    /**
     * Class that wraps the different possible options of search and provides various useful constructors.
     */
    class SearchOptions {

        private boolean searchNodes;
        private Node[] nodesToSearch;
        private Edge[] edgesToSearch;
        private Integer startingRow = null, startingColumn = null;
        private HashSet<Integer> columnsToSearch = new HashSet<>();
        private boolean loopToBeginning = true;
        private Pattern regexPattern;
        private boolean useRegexReplaceMode = false;
        private int regionStart = 0;
        private boolean onlyMatchWholeAttributeValue;

        public void resetStatus() {
            regionStart = 0;
            startingRow = null;
            startingColumn = null;
        }

        /**
         * Sets nodesToSearch as all nodes in the graph if they are null or empty array.
         * Also only search on visible view if data table is showing visible only.
         */
        private void checkNodesToSearch() {
            if (nodesToSearch == null || nodesToSearch.length == 0) {
                Graph graph;
                if (Lookup.getDefault().lookup(DataTablesController.class).isShowOnlyVisible()) {
                    graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraphVisible();
                } else {
                    graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                }
                nodesToSearch = graph.getNodes().toArray();
            }
        }

        /**
         * Sets edgesToSearch as all edges in the graph if they are null or empty array.
         * Also only search on visible view if data table is showing visible only.
         */
        private void checkEdgesToSearch() {
            if (edgesToSearch == null || edgesToSearch.length == 0) {
                Graph hg;
                if (Lookup.getDefault().lookup(DataTablesController.class).isShowOnlyVisible()) {
                    hg = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraphVisible();
                } else {
                    hg = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                }
                edgesToSearch = hg.getEdges().toArray();
            }
        }

        /**
         * Setup options to search on nodes with the given pattern.
         * If nodesToSearch is null, all nodes of the graph will be used.
         * @param nodesToSearch
         * @param regexPattern
         */
        public SearchOptions(Node[] nodesToSearch, Pattern regexPattern) {
            this.nodesToSearch = nodesToSearch;
            this.regexPattern = regexPattern;
            searchNodes = true;
            checkNodesToSearch();
        }

        /**
         * Setup options to search on edges with the given pattern.
         * If edgesToSearch is null, all edges of the graph will be used.
         * @param edgesToSearch
         * @param regexPattern
         */
        public SearchOptions(Edge[] edgesToSearch, Pattern regexPattern) {
            this.edgesToSearch = edgesToSearch;
            this.regexPattern = regexPattern;
            searchNodes = false;
            checkEdgesToSearch();
        }

        /**
         * Setup options to search on nodes with the given pattern.
         * If nodesToSearch is null, all nodes of the graph will be used.
         * @param nodesToSearch
         * @param regexPattern
         * @param onlyMatchWholeAttributeValue 
         */
        public SearchOptions(Node[] nodesToSearch, Pattern regexPattern, boolean onlyMatchWholeAttributeValue) {
            this.nodesToSearch = nodesToSearch;
            this.regexPattern = regexPattern;
            this.onlyMatchWholeAttributeValue = onlyMatchWholeAttributeValue;
            searchNodes = true;
        }

        /**
         * Setup options to search on edges with the given pattern.
         * If edgesToSearch is null, all edges of the graph will be used.
         * @param edgesToSearch
         * @param regexPattern
         * @param onlyMatchWholeAttributeValue
         */
        public SearchOptions(Edge[] edgesToSearch, Pattern regexPattern, boolean onlyMatchWholeAttributeValue) {
            this.edgesToSearch = edgesToSearch;
            this.regexPattern = regexPattern;
            this.onlyMatchWholeAttributeValue = onlyMatchWholeAttributeValue;
            searchNodes = false;
        }

        /************Getters and setters***********/
        public Edge[] getEdgesToSearch() {
            return edgesToSearch;
        }

        public Node[] getNodesToSearch() {
            return nodesToSearch;
        }

        public boolean isOnlyMatchWholeAttributeValue() {
            return onlyMatchWholeAttributeValue;
        }

        public void setOnlyMatchWholeAttributeValue(boolean onlyMatchWholeAttributeValue) {
            this.onlyMatchWholeAttributeValue = onlyMatchWholeAttributeValue;
        }

        public Pattern getRegexPattern() {
            return regexPattern;
        }

        public void setRegexPattern(Pattern regexPattern) {
            this.regexPattern = regexPattern;
        }

        public Integer getStartingColumn() {
            return startingColumn;
        }

        public void setStartingColumn(Integer startingColumn) {
            this.startingColumn = startingColumn;
        }

        public Integer getStartingRow() {
            return startingRow;
        }

        public void setStartingRow(Integer startingRow) {
            this.startingRow = startingRow;
        }

        /**
         * Set column indexes that should be used to search with the current options.
         * If columnsToSearch is empty, all columns will be used to search.
         * @param columnsToSearch It is safe to specify invalid columns indexes, they will be ignored
         */
        public void setColumnsToSearch(int[] columnsToSearch) {
            this.columnsToSearch.clear();
            if (columnsToSearch != null) {
                for (Integer i : columnsToSearch) {
                    this.columnsToSearch.add(i);
                }
            }
        }

        /**
         * Set column that should be used to search with the current options.
         * If columnsToSearch is empty, all columns will be used to search.
         * @param columnsToSearch It is safe to specify invalid columns, they will be ignored
         */
        public void setColumnsToSearch(Column[] columnsToSearch) {
            this.columnsToSearch.clear();
            if (columnsToSearch != null) {
                for (Column c : columnsToSearch) {
                    this.columnsToSearch.add(c.getIndex());
                }
            }
        }

        /**
         * Returns columns indexes to search
         * @return Set with columns indexes to search
         */
        public Set<Integer> getColumnsToSearch() {
            return columnsToSearch;
        }

        public boolean isSearchNodes() {
            return searchNodes;
        }

        public int getRegionStart() {
            return regionStart;
        }

        public void setRegionStart(int regionStart) {
            this.regionStart = regionStart;
        }

        public boolean isUseRegexReplaceMode() {
            return useRegexReplaceMode;
        }

        public void setUseRegexReplaceMode(boolean useRegexReplaceMode) {
            this.useRegexReplaceMode = useRegexReplaceMode;
        }

        public boolean isLoopToBeginning() {
            return loopToBeginning;
        }

        public void setLoopToBeginning(boolean loopToBeginning) {
            this.loopToBeginning = loopToBeginning;
        }
    }

    /**
     * <p>Class that wraps the result of a search contaning the search options used for this result
     * and the node or edge, row, column and start-end index of the value where ocurrence was found.</p>
     */
    class SearchResult {

        /**
         * searchOptions for finding next match.
         */
        private SearchOptions searchOptions;
        private Node foundNode;
        private Edge foundEdge;
        private int foundRowIndex, foundColumnIndex;
        private int start, end;

        public SearchResult(SearchOptions searchOptions, Node foundNode, Edge foundEdge, int foundRowIndex, int foundColumnIndex, int start, int end) {
            this.searchOptions = searchOptions;
            this.foundNode = foundNode;
            this.foundEdge = foundEdge;
            this.foundRowIndex = foundRowIndex;
            this.foundColumnIndex = foundColumnIndex;
            this.start = start;
            this.end = end;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getFoundColumnIndex() {
            return foundColumnIndex;
        }

        public void setFoundColumnIndex(int foundColumnIndex) {
            this.foundColumnIndex = foundColumnIndex;
        }

        public Edge getFoundEdge() {
            return foundEdge;
        }

        public void setFoundEdge(Edge foundEdge) {
            this.foundEdge = foundEdge;
        }

        public Node getFoundNode() {
            return foundNode;
        }

        public void setFoundNode(Node foundNode) {
            this.foundNode = foundNode;
        }

        public int getFoundRowIndex() {
            return foundRowIndex;
        }

        public void setFoundRowIndex(int foundRowIndex) {
            this.foundRowIndex = foundRowIndex;
        }

        public SearchOptions getSearchOptions() {
            return searchOptions;
        }

        public void setSearchOptions(SearchOptions searchOptions) {
            this.searchOptions = searchOptions;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }
    }
}
