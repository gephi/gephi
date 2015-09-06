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
package org.gephi.datalab.impl;

import java.util.Set;
import java.util.regex.Matcher;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.api.SearchReplaceController;
import org.gephi.datalab.api.SearchReplaceController.SearchResult;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the SearchReplaceController interface
 * declared in the Data Laboratory API.
 * @see SearchReplaceController
 * @author Eduardo Ramos
 */
@ServiceProvider(service = SearchReplaceController.class)
public class SearchReplaceControllerImpl implements SearchReplaceController {

    @Override
    public SearchResult findNext(SearchOptions searchOptions) {
        int row = 0;
        int column = 0;
        if (searchOptions.getStartingRow() != null) {
            row = searchOptions.getStartingRow();
        }
        if (searchOptions.getStartingColumn() != null) {
            column = searchOptions.getStartingColumn();
        }
        SearchResult result = null;
        if (searchOptions.isSearchNodes()) {
            result = findOnNodes(searchOptions, row, column);
            if (result == null && searchOptions.isLoopToBeginning()) {
                searchOptions.resetStatus();
                return findOnNodes(searchOptions, 0, 0);//If the end of data is reached with no success, try to search again from the beginning as a loop
            } else {
                return result;
            }
        } else {
            result = findOnEdges(searchOptions, row, column);
            if (result == null && searchOptions.isLoopToBeginning()) {
                searchOptions.resetStatus();
                return findOnEdges(searchOptions, 0, 0);//If the end of data is reached with no success, try to search again from the beginning as a loop
            } else {
                return result;
            }
        }
    }

    @Override
    public SearchResult findNext(SearchResult result) {
        return findNext(result.getSearchOptions());
    }

    @Override
    public boolean canReplace(SearchResult result) {
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Table table;
        Column column;
        if (result.getFoundNode() != null) {
            table = gc.getGraphModel().getNodeTable();
            column = table.getColumn(result.getFoundColumnIndex());
        } else {
            table = gc.getGraphModel().getEdgeTable();
            column = table.getColumn(result.getFoundColumnIndex());
        }
        return Lookup.getDefault().lookup(AttributeColumnsController.class).canChangeColumnData(column);
    }

    @Override
    public SearchResult replace(SearchResult result, String replacement) {
        if (result == null) {
            throw new IllegalArgumentException();
        }
        if (!canReplace(result)) {
            //Data has changed and the replacement can't be done, continue looking.
            return findNext(result);//Go to next search result
        }
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Object value;
        String str;
        Element attributes;
        Column column;

        if (!result.getSearchOptions().isUseRegexReplaceMode()) {
            replacement = Matcher.quoteReplacement(replacement);//Avoid using groups and other regex aspects in the replacement
        }

        try {
            //Get value to re-match and replace:
            if (result.getFoundNode() != null) {
                attributes = result.getFoundNode();
                column = gc.getGraphModel().getNodeTable().getColumn(result.getFoundColumnIndex());
            } else {
                attributes = result.getFoundEdge();
                column = gc.getGraphModel().getEdgeTable().getColumn(result.getFoundColumnIndex());
            }
            value = attributes.getAttribute(column);
            str = value != null ? value.toString() : "";
            StringBuffer sb = new StringBuffer();

            //Match and replace the result:
            Matcher matcher = result.getSearchOptions().getRegexPattern().matcher(str.substring(result.getStart()));
            if (matcher.find()) {
                matcher.appendReplacement(sb, replacement);
                int replaceLong = sb.length();
                matcher.appendTail(sb);
                str = str.substring(0, result.getStart()) + sb.toString();

                result.getSearchOptions().setRegionStart(result.getStart() + replaceLong);
                Lookup.getDefault().lookup(AttributeColumnsController.class).setAttributeValue(str, attributes, column);
                return findNext(result);//Go to next search result
            } else {
                //Data has changed and the replacement can't be done, continue looking.
                return findNext(result);//Go to next search result
            }
        } catch (Exception ex) {
            if (ex instanceof IndexOutOfBoundsException) {
                throw new IndexOutOfBoundsException();//Rethrow the exception when it is caused by a bad regex replacement
            }
            //Data has changed (a lot of different errors can happen) and the replacement can't be done, continue looking.
            return findNext(result);//Go to next search result
        }
    }

    @Override
    public int replaceAll(SearchOptions searchOptions, String replacement) {
        int replacementsCount = 0;
        searchOptions.resetStatus();
        searchOptions.setLoopToBeginning(false);//To avoid infinite loop when the replacement parse makes it to match again.
        SearchResult result;
        result = findNext(searchOptions);
        while (result != null) {
            if (canReplace(result)) {
                result = replace(result, replacement);
                replacementsCount++;
            } else {
                result = findNext(searchOptions);
            }
        }
        searchOptions.setLoopToBeginning(true);//Restore loop behaviour
        return replacementsCount;
    }

    private SearchResult findOnNodes(SearchOptions searchOptions, int rowIndex, int columnIndex) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        SearchResult result = null;
        Set<Integer> columnsToSearch = searchOptions.getColumnsToSearch();
        boolean searchAllColumns = columnsToSearch.isEmpty();
        Table table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
        Node[] nodes = searchOptions.getNodesToSearch();
        Node row;
        Column column;
        Object value;
        
        for (; rowIndex < nodes.length; rowIndex++) {
            if (!gec.isNodeInGraph(nodes[rowIndex])) {
                continue;//Make sure node is still in graph when continuing a search
            }
            row = nodes[rowIndex];
            for (; columnIndex < table.countColumns(); columnIndex++) {
                if (searchAllColumns || columnsToSearch.contains(columnIndex)) {
                    column = table.getColumn(columnIndex);
                    value = row.getAttribute(column);
                    result = matchRegex(value, searchOptions, rowIndex, columnIndex);
                    if (result != null) {
                        result.setFoundNode(nodes[rowIndex]);
                        return result;
                    }
                }
                searchOptions.setRegionStart(0);//Start at the beginning for the next value
            }
            searchOptions.setRegionStart(0);//Start at the beginning for the next value
            columnIndex = 0;//Start at the first column for the next row
        }
        return result;
    }

    private SearchResult findOnEdges(SearchOptions searchOptions, int rowIndex, int columnIndex) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        SearchResult result = null;
        Set<Integer> columnsToSearch = searchOptions.getColumnsToSearch();
        boolean searchAllColumns = columnsToSearch.isEmpty();
        Table table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getEdgeTable();
        Edge[] edges = searchOptions.getEdgesToSearch();
        Edge row;
        Column column;
        Object value;
        
        for (; rowIndex < edges.length; rowIndex++) {
            if (!gec.isEdgeInGraph(edges[rowIndex])) {
                continue;//Make sure edge is still in graph when continuing a search
            }
            row = edges[rowIndex];
            for (; columnIndex < table.countColumns(); columnIndex++) {
                if (searchAllColumns || columnsToSearch.contains(columnIndex)) {
                    column = table.getColumn(columnIndex);
                    value = row.getAttribute(column);
                    result = matchRegex(value, searchOptions, rowIndex, columnIndex);
                    if (result != null) {
                        result.setFoundEdge(edges[rowIndex]);
                        return result;
                    }
                }
                searchOptions.setRegionStart(0);//Start at the beginning for the next value
            }
            searchOptions.setRegionStart(0);//Start at the beginning for the next value
            columnIndex = 0;//Start at the first column for the next row
        }
        return result;
    }

    private SearchResult matchRegex(Object value, SearchOptions searchOptions, int rowIndex, int columnIndex) {
        boolean found;
        String str = value != null ? value.toString() : "";
        Matcher matcher = searchOptions.getRegexPattern().matcher(str);
        if (str.isEmpty()) {
            if (searchOptions.getRegionStart() > 0) {
                return null;
            }
        } else if (searchOptions.getRegionStart() >= str.length()) {
            return null;//No more to search in this value, go to next
        }

        if (searchOptions.isOnlyMatchWholeAttributeValue()) {
            found = matcher.matches();//Try to match the whole value
        } else {
            matcher.region(searchOptions.getRegionStart(), str.length());//Try to match a group in the remaining part of the value
            found = matcher.find();
        }

        if (found) {
            searchOptions.setStartingRow(rowIndex);//For next search
            searchOptions.setStartingColumn(columnIndex);//For next search
            int end = matcher.end();
            if (matcher.start() == end && !str.isEmpty()) {
                return null;//Do not match empty string in not empty values
            }
            if (str.isEmpty()) {
                end++;//To be able to search on next values when the value matched is empty
            }
            searchOptions.setRegionStart(end);//Start next search after this match in this value. (If it is greater than the length of the value, it will be discarded at the beginning of this method next time)
            return new SearchResult(searchOptions, null, null, rowIndex, columnIndex, matcher.start(), matcher.end());//Set node or edge values later
        } else {
            return null;
        }
    }
}
