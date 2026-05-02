package org.gephi.desktop.attributes;

import java.beans.PropertyChangeEvent;

public class AttributesUIModelEvent extends PropertyChangeEvent {

    public static String MODEL = "model";
    public static String EDIT_MODE = "mode";
    public static String HIDDEN_COLUMN_IDS = "hiddenColumnIds";
    public static String SELECTED_ELEMENTS = "selectedElements";
    public static String SHOW_NULL_COLUMNS = "showNullColumns";
    public static String INCLUDE_PROPERTIES = "includeProperties";

    public AttributesUIModelEvent(Object source, String propertyName,
                                  Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }
}
