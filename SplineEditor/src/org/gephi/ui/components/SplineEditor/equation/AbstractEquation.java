/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.ui.components.SplineEditor.equation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractEquation implements Equation {

    protected List<PropertyChangeListener> listeners;

    protected AbstractEquation() {
        this.listeners = new LinkedList<PropertyChangeListener>();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    protected void firePropertyChange(String propertyName,
            double oldValue,
            double newValue) {
        PropertyChangeEvent changeEvent = new PropertyChangeEvent(this,
                propertyName,
                oldValue,
                newValue);
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(changeEvent);
        }
    }
}
