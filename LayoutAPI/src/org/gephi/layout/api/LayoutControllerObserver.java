/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.api;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public interface LayoutControllerObserver {

    public void executeLayoutEvent();

    public void stopLayoutEvent();
}
