/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.api;

import java.util.regex.Pattern;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 * <p>Independent controller for search/replace feature.</p>
 * <p>Operates with <code>SearchOptions</code> and <code>SearchResult</code> objects.</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
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
        private boolean loopToBeginning = true;
        private Pattern regexPattern;
        private boolean useRegexReplaceMode = false;
        private int regionStart = 0;
        private boolean onlyMatchWholeAttributeValue;

        public void resetStatus() {
            regionStart = 0;
            startingRow = null;
            startingRow = null;
        }

        /**
         * Sets nodesToSearch as all nodes in the graph if they are null or empty array.
         * Also only search on visible view if data table is showing visible only.
         */
        private void checkNodesToSearch() {
            if (nodesToSearch == null || nodesToSearch.length == 0) {
                HierarchicalGraph hg;
                if (Lookup.getDefault().lookup(DataTablesController.class).isShowOnlyVisible()) {
                    hg = Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraphVisible();
                } else {
                    hg = Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph();
                }
                nodesToSearch = hg.getNodesTree().toArray();
            }
        }

        /**
         * Sets edgesToSearch as all edges in the graph if they are null or empty array.
         * Also only search on visible view if data table is showing visible only.
         */
        private void checkEdgesToSearch() {
            if (edgesToSearch == null || edgesToSearch.length == 0) {
                HierarchicalGraph hg;
                if (Lookup.getDefault().lookup(DataTablesController.class).isShowOnlyVisible()) {
                    hg = Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraphVisible();
                } else {
                    hg = Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph();
                }
                edgesToSearch = hg.getEdges().toArray();
            }
        }

        /*******Available constructors*********/
        public SearchOptions(Node[] nodesToSearch, Pattern regexPattern) {
            this.nodesToSearch = nodesToSearch;
            this.regexPattern = regexPattern;
            searchNodes = true;
            checkNodesToSearch();
        }

        public SearchOptions(Edge[] edgesToSearch, Pattern regexPattern) {
            this.edgesToSearch = edgesToSearch;
            this.regexPattern = regexPattern;
            searchNodes = false;
            checkEdgesToSearch();
        }

        public SearchOptions(Node[] nodesToSearch, Pattern regexPattern, boolean onlyMatchWholeAttributeValue) {
            this.nodesToSearch = nodesToSearch;
            this.regexPattern = regexPattern;
            this.onlyMatchWholeAttributeValue = onlyMatchWholeAttributeValue;
            searchNodes = true;
        }

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
