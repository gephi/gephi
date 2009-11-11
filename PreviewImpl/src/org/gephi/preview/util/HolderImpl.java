package org.gephi.preview.util;

import org.gephi.preview.api.util.Holder;

/**
 * Implementation of a generic component holder.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class HolderImpl<T> implements Holder<T> {

    private T component;

    /**
     * Returns the hold component.
     *
     * @return the hold component
     */
    public T getComponent() {
        return component;
    }

    /**
     * Defines the hold component.
     *
     * @param component  the component to bind
     */
    public void setComponent(T component) {
        this.component = component;
    }
}
