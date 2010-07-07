package org.gephi.neo4j.api;

/**
 *
 * @author Martin Škurla
 */
public class FilterDescription {
    private final String propertyKey;
    private final FilterOperator operator;
    private final String propertyValue;

    
    public FilterDescription(String propertyKey, FilterOperator operator, String propertyValue) {
        this.propertyKey = propertyKey;
        this.operator = operator;
        this.propertyValue = propertyValue;
    }


    public FilterOperator getOperator() {
        return operator;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
