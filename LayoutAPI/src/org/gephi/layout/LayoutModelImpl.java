/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutModelImpl implements LayoutModel {

    //Listeners
    private List<PropertyChangeListener> listeners;
    //Data
    private Layout selectedLayout;
    private LayoutBuilder selectedBuilder;
    private Map<LayoutPropertyKey, Object> savedProperties;
    //Util
    private LongTaskExecutor executor;

    public LayoutModelImpl() {
        listeners = new ArrayList<PropertyChangeListener>();
        savedProperties = new HashMap<LayoutPropertyKey, Object>();

        executor = new LongTaskExecutor(true, "layout", 5);
        executor.setLongTaskListener(new LongTaskListener() {

            public void taskFinished(LongTask task) {
                setRunning(false);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        });
    }

    public Layout getSelectedLayout() {
        return selectedLayout;
    }

    public LayoutBuilder getSelectedBuilder() {
        return selectedBuilder;
    }

    public Layout getLayout(LayoutBuilder layoutBuilder) {
        Layout layout = layoutBuilder.buildLayout();
        selectedBuilder = layoutBuilder;
        layout.resetPropertiesValues();
        return layout;
    }

    protected void setSelectedLayout(Layout selectedLayout) {
        Layout oldValue = this.selectedLayout;
        this.selectedLayout = selectedLayout;
        this.selectedBuilder = selectedLayout != null ? selectedLayout.getBuilder() : null;
        if (oldValue != null) {
            saveProperties(oldValue);
        }
        if (selectedLayout != null) {
            loadProperties(selectedLayout);
        }
        firePropertyChangeEvent(SELECTED_LAYOUT, oldValue, selectedLayout);
    }

    public boolean isRunning() {
        return executor.isRunning();
    }

    protected void setRunning(boolean running) {
        firePropertyChangeEvent(RUNNING, !running, running);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent evt = null;
        if (propertyName.equals(SELECTED_LAYOUT)) {
            evt = new PropertyChangeEvent(this, SELECTED_LAYOUT, oldValue, newValue);
        } else if (propertyName.equals(RUNNING)) {
            evt = new PropertyChangeEvent(this, RUNNING, oldValue, newValue);
        } else {
            return;
        }
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }

    public LongTaskExecutor getExecutor() {
        return executor;
    }

    public void saveProperties(Layout layout) {
        for (LayoutProperty p : layout.getProperties()) {
            try {
                Object value = p.getProperty().getValue();
                if (value != null) {
                    savedProperties.put(new LayoutPropertyKey(p.getProperty().getName(), layout.getClass().getName()), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadProperties(Layout layout) {
        List<LayoutPropertyKey> layoutValues = new ArrayList<LayoutPropertyKey>();
        for (LayoutPropertyKey val : savedProperties.keySet()) {
            if (val.layoutClassName.equals(layout.getClass().getName())) {
                layoutValues.add(val);
            }
        }
        for (LayoutProperty property : layout.getProperties()) {
            for (LayoutPropertyKey l : layoutValues) {
                if (property.getProperty().getName().equals(l.name)) {
                    try {
                        property.getProperty().setValue(savedProperties.get(l));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class LayoutPropertyKey {

        private volatile int hashCode = 0;      //Cache hashcode
        private final String name;
        private final String layoutClassName;

        public LayoutPropertyKey(String name, String layoutClassName) {
            this.name = name;
            this.layoutClassName = layoutClassName;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LayoutPropertyKey)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            LayoutPropertyKey s = (LayoutPropertyKey) obj;
            if (s.layoutClassName.equals(layoutClassName) && s.name.equals(name)) {
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash += 53 * layoutClassName.hashCode();
                hash += 53 * name.hashCode();
                hashCode = hash;
            }
            return hashCode;
        }
    }

    public Element writeXML(Document document) {
        Element layoutModelE = document.createElement("layoutmodel");

        if (selectedLayout != null) {
            saveProperties(selectedLayout);
        }

        //Properties
        Element propertiesE = document.createElement("properties");
        for (Entry<LayoutPropertyKey, Object> entry : savedProperties.entrySet()) {
            Element propertyE = document.createElement("property");
            propertyE.setAttribute("layout", entry.getKey().layoutClassName);
            propertyE.setAttribute("property", entry.getKey().name);
            propertyE.setAttribute("class", entry.getValue().getClass().getName());
            propertyE.setTextContent(entry.getValue().toString());
            propertiesE.appendChild(propertyE);
        }

        layoutModelE.appendChild(propertiesE);

        return layoutModelE;
    }

    public void readXML(Element layoutModelElement) {
        NodeList propertyList = layoutModelElement.getElementsByTagName("property");
        for (int i = 0; i < propertyList.getLength(); i++) {
            Node n = propertyList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element propertyE = (Element) n;

                LayoutPropertyKey key = new LayoutPropertyKey(propertyE.getAttribute("property"), propertyE.getAttribute("layout"));
                Object value = parse(propertyE.getAttribute("class"), propertyE.getTextContent());
                savedProperties.put(key, value);
            }
        }
    }

    private Object parse(String classStr, String str) {
        try {
            Class c = Class.forName(classStr);
            if (c.equals(Boolean.class)) {
                return new Boolean(str);
            } else if (c.equals(Integer.class)) {
                return new Integer(str);
            } else if (c.equals(Float.class)) {
                return new Float(str);
            } else if (c.equals(Double.class)) {
                return new Double(str);
            } else if (c.equals(Long.class)) {
                return new Long(str);
            } else if (c.equals(String.class)) {
                return str;
            }
        } catch (ClassNotFoundException ex) {
            return null;
        }
        return null;
    }
}
