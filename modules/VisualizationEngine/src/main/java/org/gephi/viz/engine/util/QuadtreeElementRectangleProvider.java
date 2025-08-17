package org.gephi.viz.engine.util;

/**
 *
 * @author Eduardo Ramos
 * @param <T>
 */
public interface QuadtreeElementRectangleProvider<T> {

    float minX(T element);

    float minY(T element);

    float maxX(T element);

    float maxY(T element);
}
