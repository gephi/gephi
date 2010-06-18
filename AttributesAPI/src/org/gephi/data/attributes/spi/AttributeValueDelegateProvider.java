package org.gephi.data.attributes.spi;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.properties.PropertiesColumn;


/**
 * Provider for delegating
 *
 * @author Martin Škurla
 */
public interface AttributeValueDelegateProvider {
    //PropertiesColumn getDelegateIdColumn();
    Object getNodeValue(AttributeColumn attributeColumn, Object id);
    Object getEdgeValue(AttributeColumn attributeColumn, Object id);
}
