package org.gephi.neo4j.api;

/**
 *
 * @author Martin Škurla
 */
public class FilterInfo {
    private final String propertyKey;
    private final String operator;
    private final Object propertyValue;

    
    public FilterInfo(String propertyKey, String operator, Object propertyValue) {
        this.propertyKey = propertyKey;
        this.operator = operator;
        this.propertyValue = propertyValue;
    }


    public String getOperator() {
        return operator;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }
}
