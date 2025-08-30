package org.gephi.visualization.api;

public interface VisualizationEvent {

    Type getType();

    Object getData();

    enum Type {
        START_DRAG,
        DRAG,
        STOP_DRAG,
        MOUSE_MOVE,
        MOUSE_LEFT_PRESS,
        MOUSE_MIDDLE_PRESS,
        MOUSE_RIGHT_PRESS,
        MOUSE_LEFT_CLICK,
        MOUSE_MIDDLE_CLICK,
        MOUSE_RIGHT_CLICK,
        MOUSE_LEFT_PRESSING,
        MOUSE_RELEASED,
        NODE_LEFT_CLICK,
        NODE_LEFT_PRESS,
        NODE_LEFT_PRESSING,
    }
}
