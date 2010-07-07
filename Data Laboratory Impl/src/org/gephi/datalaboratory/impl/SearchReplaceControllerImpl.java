/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl;

import java.util.regex.Matcher;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalaboratory.api.SearchReplaceController;
import org.gephi.datalaboratory.api.SearchReplaceController.SearchResult;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = SearchReplaceController.class)
public class SearchReplaceControllerImpl implements SearchReplaceController {

    public SearchResult findNext(SearchOptions searchOptions) {
        int row = 0;
        int column = 0;
        if (searchOptions.getStartingRow() != null) {
            row = searchOptions.getStartingRow();
        }
        if (searchOptions.getStartingColumn() != null) {
            column = searchOptions.getStartingColumn();
        }
        if (searchOptions.isSearchNodes()) {
            return findOnNodes(searchOptions, row, column);
        } else {
            return findOnEdges(searchOptions, row, column);
        }
    }

    public SearchResult findNext(SearchResult result) {
        return findNext(result.getSearchOptions());
    }

    public boolean canReplace(SearchResult result) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeColumn column;
        if (result.getFoundNode() != null) {
            column = ac.getModel().getNodeTable().getColumn(result.getFoundColumnIndex());
            return column.getOrigin() != AttributeOrigin.COMPUTED && column.getIndex() != PropertiesColumn.NODE_ID.getIndex();
        } else {
            column = ac.getModel().getEdgeTable().getColumn(result.getFoundColumnIndex());
            return column.getOrigin() != AttributeOrigin.COMPUTED && column.getIndex() != PropertiesColumn.EDGE_ID.getIndex();
        }
    }

    public SearchResult replace(SearchResult result, String replacement) {
        if (!canReplace(result) || result == null) {
            throw new IllegalArgumentException();
        }
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        Object value;
        String str;
        Attributes attributes;
        AttributeType type;

        if(!result.getSearchOptions().isUseRegexReplaceMode()){
            replacement=Matcher.quoteReplacement(replacement);//Avoid using groups and other regex aspects in the replacement
        }

        //Get value to re-match and replace:
        if (result.getFoundNode() != null) {
            attributes = result.getFoundNode().getNodeData().getAttributes();
            type = ac.getModel().getNodeTable().getColumn(result.getFoundColumnIndex()).getType();
        } else {
            attributes = result.getFoundEdge().getEdgeData().getAttributes();
            type = ac.getModel().getEdgeTable().getColumn(result.getFoundColumnIndex()).getType();
        }
        value = attributes.getValue(result.getFoundColumnIndex());
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
            try {
                value = type.parse(str);
            } catch (Exception ex) {
                value = null;
            }
            attributes.setValue(result.getFoundColumnIndex(), value);
            return findNext(result);//Go to next search result
        } else {
            throw new IllegalArgumentException();//Could not match, given SearchResult was not correct
        }
    }

    public void replaceAll(SearchOptions searchOptions, String replacement) {
        searchOptions.resetStatus();
        SearchResult result;
        result = findNext(searchOptions);
        while (result != null) {
            if (canReplace(result)) {
                result = replace(result, replacement);
            } else {
                result = findNext(searchOptions);
            }
        }
    }

    private SearchResult findOnNodes(SearchOptions searchOptions, int rowIndex, int columnIndex) {
        SearchResult result = null;
        Node[] nodes = searchOptions.getNodesToSearch();
        AttributeRow row;
        Object value;
        for (; rowIndex < nodes.length; rowIndex++) {
            row = (AttributeRow) nodes[rowIndex].getNodeData().getAttributes();
            for (; columnIndex < row.countValues(); columnIndex++) {
                value = row.getValue(columnIndex);
                result = matchRegex(value, searchOptions, rowIndex, columnIndex);
                if (result != null) {
                    result.setFoundNode(nodes[rowIndex]);
                    return result;
                }
                searchOptions.setRegionStart(0);//Start at the beginning for the next value
            }
            columnIndex = 0;//Start at the first column for the next row
        }
        return result;
    }

    private SearchResult findOnEdges(SearchOptions searchOptions, int rowIndex, int columnIndex) {
        SearchResult result = null;
        Edge[] edges = searchOptions.getEdgesToSearch();
        AttributeRow row;
        Object value;
        for (; rowIndex < edges.length; rowIndex++) {
            row = (AttributeRow) edges[rowIndex].getEdgeData().getAttributes();
            for (; columnIndex < row.countValues(); columnIndex++) {
                value = row.getValue(columnIndex);
                result = matchRegex(value, searchOptions, rowIndex, columnIndex);
                if (result != null) {
                    result.setFoundEdge(edges[rowIndex]);
                    return result;
                }
                searchOptions.setRegionStart(0);//Start at the beginning for the next value
            }
            columnIndex = 0;//Start at the first column for the next row
        }
        return result;
    }

    private SearchResult matchRegex(Object value, SearchOptions searchOptions, int rowIndex, int columnIndex) {
        boolean found;
        String str = value != null ? value.toString() : "";
        Matcher matcher = searchOptions.getRegexPattern().matcher(str);
        if (searchOptions.getRegionStart() >= str.length()) {
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
            searchOptions.setRegionStart(matcher.end() + 1);//Start next search after this match in this value. (If it is greater than the length of the value, it will be discarded at the beginning of this method next time)
            return new SearchResult(searchOptions, null, null, rowIndex, columnIndex, matcher.start(), matcher.end());//Set node or edge values later
        } else {
            return null;
        }
    }
}
