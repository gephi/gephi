package org.gephi.desktop.search.spi;

public interface SearchActionProvider<T> {

    boolean canAction(T result);

    void action(T result);

    String getDescription(T result);
}
