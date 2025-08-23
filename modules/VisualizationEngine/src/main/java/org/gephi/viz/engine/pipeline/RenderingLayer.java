package org.gephi.viz.engine.pipeline;

/**
 *
 * @author Eduardo Ramos
 */
public enum RenderingLayer {
    BACK1,
    BACK2,
    BACK3,
    BACK4,
    MIDDLE1,
    MIDDLE2,
    MIDDLE3,
    MIDDLE4,
    FRONT1,
    FRONT2,
    FRONT3,
    FRONT4;

    public int getLevel() {
        switch (this) {
            case BACK1:
            case MIDDLE1:
            case FRONT1:
                return 1;
            case BACK2:
            case MIDDLE2:
            case FRONT2:
                return 2;
            case BACK3:
            case MIDDLE3:
            case FRONT3:
                return 3;
            default:
                return 4;
        }
    }

    public boolean isBack() {
        return this == BACK1 || this == BACK2 || this == BACK3 || this == BACK4;
    }

    public boolean isMiddle() {
        return this == MIDDLE1 || this == MIDDLE2 || this == MIDDLE3 || this == MIDDLE4;
    }

    public boolean isFront() {
        return this == FRONT1 || this == FRONT2 || this == FRONT3 || this == FRONT4;
    }
}
