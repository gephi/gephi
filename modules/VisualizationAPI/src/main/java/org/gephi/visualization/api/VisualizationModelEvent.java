package org.gephi.visualization.api;

import java.beans.PropertyChangeEvent;

public class VisualizationModelEvent extends PropertyChangeEvent {

    public static String MODEL = "model";
    public static String SELECTED_ELEMENT_CLASS = "selectedElementClass";
    public static String SELECTED_CATEGORY = "selectedCategory";
    public static String SELECTED_TRANSFORMER_UI = "selectedTransformerUI";
    public static String SELECTED_FUNCTION = "selectedFunction";
    public static String ATTRIBUTE_LIST = "attributeList";
    public static String REFRESH_FUNCTION = "refreshFunction";
    public static String START_STOP_AUTO_APPLY = "startStopAutoApply";
    public static String SET_AUTO_APPLY = "setStopAutoApply";
    public static String SET_LOCAL_SCALE = "setLocalScale";
    public static String SET_TRANSFORM_NULL_VALUES = "transformNullValues";

    public VisualizationModelEvent(Object source, String propertyName,
                                  Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }
}
