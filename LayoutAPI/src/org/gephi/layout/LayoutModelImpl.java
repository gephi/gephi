/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;

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
                    savedProperties.put(new LayoutPropertyKey(p.getCanonicalName(), layout.getClass().getName()), value);
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
                if (property.getCanonicalName().equalsIgnoreCase(l.name) 
                        || property.getProperty().getName().equalsIgnoreCase(l.name)) {//Also compare with property name to maintain compatibility with old saved properties
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

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("layoutmodel");

        if (selectedLayout != null) {
            saveProperties(selectedLayout);
        }

        //Properties
        writer.writeStartElement("properties");
        for (Entry<LayoutPropertyKey, Object> entry : savedProperties.entrySet()) {
            if (entry.getValue() != null) {
                writer.writeStartElement("property");
                writer.writeAttribute("layout", entry.getKey().layoutClassName);
                writer.writeAttribute("property", entry.getKey().name);
                writer.writeAttribute("class", entry.getValue().getClass().getName());
                writer.writeCharacters(entry.getValue().toString());
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();

        writer.writeEndElement();
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        boolean end = false;
        LayoutPropertyKey key = null;
        String classStr = null;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("property".equalsIgnoreCase(name)) {
                    key = new LayoutPropertyKey(reader.getAttributeValue(null, "property"), reader.getAttributeValue(null, "layout"));
                    classStr = reader.getAttributeValue(null, "class");
                }
            } else if (eventType.equals(XMLEvent.CHARACTERS)) {
                if (key != null && !reader.isWhiteSpace()) {
                    Object value = parse(classStr, reader.getText());
                    if (value != null) {
                        savedProperties.put(key, value);
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                key = null;
                if ("layoutmodel".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
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
