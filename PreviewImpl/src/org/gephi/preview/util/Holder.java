package org.gephi.preview.util;

/**
 *
 * @author jeremy
 */
public class Holder<T> {

    private T m_component;

    public final T getComponent() {
        return m_component;
    }

    public final void setComponent(T component) {
        m_component = component;
    }
}
