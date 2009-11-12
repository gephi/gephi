package org.gephi.preview.api.util;

/**
 * Interface of a generic component holder.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Holder<T> {

    /**
     * Returns the hold component.
     *
     * @return the hold component
     */
    public T getComponent();
}
