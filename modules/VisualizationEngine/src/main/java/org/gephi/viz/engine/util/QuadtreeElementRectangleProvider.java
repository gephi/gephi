package org.gephi.viz.engine.util;

/**
 *
 * @param <T>
 * @author Eduardo Ramos
 */
public interface QuadtreeElementRectangleProvider<T> {

    float minX(T element);

    float minY(T element);

    float maxX(T element);

    float maxY(T element);
}
