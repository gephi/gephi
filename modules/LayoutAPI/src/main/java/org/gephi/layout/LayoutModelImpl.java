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
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.Model;
import org.gephi.utils.Serialization;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class LayoutModelImpl implements LayoutModel, Model {

    //Listeners
    private final List<PropertyChangeListener> listeners;
    //Data
    private final Map<LayoutPropertyKey, Object> savedProperties;
    private final Workspace workspace;
    //Util
    private final LongTaskExecutor executor;
    private Layout selectedLayout;
    private LayoutBuilder selectedBuilder;

    public LayoutModelImpl(Workspace workspace) {
        this.workspace = workspace;
        listeners = new ArrayList<>();
        savedProperties = new HashMap<>();

        executor = new LongTaskExecutor(true, "layout", 5);
        executor.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskFinished(LongTask task) {
                setRunning(false);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                Exceptions.printStackTrace(t);
            }
        });
    }

    @Override
    public Layout getSelectedLayout() {
        return selectedLayout;
    }

    protected void setSelectedLayout(Layout selectedLayout) {
        Layout oldValue = this.selectedLayout;
        this.selectedLayout = selectedLayout;
        this.selectedBuilder = selectedLayout != null ? selectedLayout.getBuilder() : null;
        if (oldValue != null) {
            saveProperties(oldValue);
        }

        injectGraph();
        if (selectedLayout != null) {
            boolean onlyDefaults = loadProperties(selectedLayout);
            firePropertyChangeEvent(SELECTED_LAYOUT, oldValue, selectedLayout);
            if (onlyDefaults) {
                firePropertyChangeEvent(DEFAULTS_APPLIED, null, null);
            }
        } else {
            firePropertyChangeEvent(SELECTED_LAYOUT, oldValue, null);
        }
    }

    @Override
    public LayoutBuilder getSelectedBuilder() {
        return selectedBuilder;
    }

    @Override
    public Layout getLayout(LayoutBuilder layoutBuilder) {
        Layout layout = layoutBuilder.buildLayout();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        layout.setGraphModel(graphModel);
        selectedBuilder = layoutBuilder;
        layout.resetPropertiesValues();
        return layout;
    }

    public void injectGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (selectedLayout != null && graphController.getGraphModel(workspace) != null) {
            selectedLayout.setGraphModel(graphController.getGraphModel(workspace));
        }
    }

    @Override
    public boolean isRunning() {
        return executor.isRunning();
    }

    protected void setRunning(boolean running) {
        firePropertyChangeEvent(RUNNING, !running, running);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent evt = null;
        switch (propertyName) {
            case SELECTED_LAYOUT:
                evt = new PropertyChangeEvent(this, SELECTED_LAYOUT, oldValue, newValue);
                break;
            case RUNNING:
                evt = new PropertyChangeEvent(this, RUNNING, oldValue, newValue);
                break;
            case DEFAULTS_APPLIED:
                evt = new PropertyChangeEvent(this, DEFAULTS_APPLIED, oldValue, newValue);
                break;
            default:
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
                    if (value instanceof Column) {
                        PropertyEditor propertyEditor = p.getProperty().getPropertyEditor();
                        value = propertyEditor.getAsText();
                    }
                    savedProperties
                        .put(new LayoutPropertyKey(p.getCanonicalName(), layout.getClass().getName()), value);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    // Returns true if only the default values were applied (no saved properties)
    public boolean loadProperties(Layout layout) {
        // In case some properties are only locally defined (like cooling in ForceAtlas)
        layout.resetPropertiesValues();

        boolean onlyDefaults = true;
        List<LayoutPropertyKey> layoutValues = new ArrayList<>();
        for (LayoutPropertyKey val : savedProperties.keySet()) {
            if (val.layoutClassName.equals(layout.getClass().getName())) {
                layoutValues.add(val);
            }
        }
        for (LayoutProperty property : layout.getProperties()) {
            for (LayoutPropertyKey l : layoutValues) {
                if (property.getCanonicalName().equalsIgnoreCase(l.name)
                    || property.getProperty().getName().equalsIgnoreCase(
                    l.name)) {//Also compare with property name to maintain compatibility with old saved properties
                    try {
                        if (property.getProperty().getValueType().isAssignableFrom(Column.class)) {
                            PropertyEditor propertyEditor = property.getProperty().getPropertyEditor();
                            propertyEditor.setAsText(savedProperties.get(l).toString());
                            onlyDefaults = false;
                        } else {
                            property.getProperty().setValue(savedProperties.get(l));
                            onlyDefaults = false;
                        }
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
        return onlyDefaults;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        if (selectedLayout != null) {
            saveProperties(selectedLayout);
            writer.writeStartElement("selectedlayoutbuilder");
            writer.writeAttribute("class", selectedLayout.getBuilder().getClass().getName());
            writer.writeEndElement();
        }

        //Properties
        writer.writeStartElement("properties");
        for (Entry<LayoutPropertyKey, Object> entry : savedProperties.entrySet()) {
            if (entry.getValue() != null) {
                writer.writeStartElement("property");
                writer.writeAttribute("layout", entry.getKey().layoutClassName);
                writer.writeAttribute("property", entry.getKey().name);
                writer.writeAttribute("class", entry.getValue().getClass().getName());
                writer.writeCharacters(Serialization.getValueAsText(entry.getValue()));
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        boolean end = false;
        LayoutPropertyKey key = null;
        String valueClassStr = null;
        String selectedLayoutBuilderClass = null;

        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("property".equalsIgnoreCase(name)) {
                    key = new LayoutPropertyKey(reader.getAttributeValue(null, "property"),
                        reader.getAttributeValue(null, "layout"));
                    valueClassStr = reader.getAttributeValue(null, "class");
                } else if ("selectedlayoutbuilder".equalsIgnoreCase(name)) {
                    selectedLayoutBuilderClass = reader.getAttributeValue(null, "class");
                }
            } else if (eventType.equals(XMLEvent.CHARACTERS)) {
                if (key != null && !reader.isWhiteSpace()) {
                    Object value = parse(valueClassStr, reader.getText());
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

        //Try to retrieve selected layout and make it the currently selected layout (after reading saved properties)
        if (selectedLayoutBuilderClass != null) {
            for (LayoutBuilder builder : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
                if (builder.getClass().getName().equals(selectedLayoutBuilderClass)) {
                    setSelectedLayout(builder.buildLayout());
                    break;
                }
            }
        }
    }

    private Object parse(String classStr, String str) {
        return Serialization.readValueFromText(str, classStr);
    }

    private static class LayoutPropertyKey {

        private final String name;
        private final String layoutClassName;
        private volatile int hashCode = 0;      //Cache hashcode

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
            return s.layoutClassName.equals(layoutClassName) && s.name.equals(name);
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
}
