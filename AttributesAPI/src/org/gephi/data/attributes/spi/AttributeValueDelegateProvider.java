package org.gephi.data.attributes.spi;


import org.gephi.data.attributes.api.AttributeColumn;


/**
 * Provider for delegating attribute value. Using this interface it is possible to delegate the real
 * value of AttributeValue object to database (relational, graph, ...), index/search engine, ...
 *
 * @author Martin Škurla
 */
public interface AttributeValueDelegateProvider {
    /**
     * Returns the delegated value of node. Delegated value is determined from given AttributeColumn
     * and id.
     *
     * @param attributeColumn attribute column
     * @param id              id
     * 
     * @return delegated value of node
     */
    Object getNodeValue(AttributeColumn attributeColumn, Object id);

    /**
     * Returns the delegated value of edge. Delegated value is determined from given AttributeColumn
     * and id.
     *
     * @param attributeColumn attribute column
     * @param id              id
     * 
     * @return delegated value of edge
     */
    Object getEdgeValue(AttributeColumn attributeColumn, Object id);
}
