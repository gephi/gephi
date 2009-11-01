package org.gephi.preview.util;

import org.gephi.preview.api.Holder;

/**
 *
 * @author jeremy
 */
public class HolderImpl<T> implements Holder<T> {

    private T m_component;

    public final T getComponent() {
        return m_component;
    }

    public final void setComponent(T component) {
        m_component = component;
    }
}
