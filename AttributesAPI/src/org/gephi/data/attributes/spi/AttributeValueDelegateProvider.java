package org.gephi.data.attributes.spi;


import org.gephi.data.attributes.api.AttributeColumn;


/**
 * Provider for delegating attribute value.
 *
 * @author Martin Škurla
 */
public interface AttributeValueDelegateProvider {
    /**
     *
     * @param attributeColumn
     * @param id
     * 
     * @return
     */
    Object getNodeValue(AttributeColumn attributeColumn, Object id);

    /**
     *
     * @param attributeColumn
     * @param id
     * 
     * @return
     */
    Object getEdgeValue(AttributeColumn attributeColumn, Object id);
}
