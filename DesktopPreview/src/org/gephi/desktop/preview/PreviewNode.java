/*
Copyright 2008-2011 Gephi
Authors : Jérémy Subtil <jeremy.subtil@gephi.org>,
          Yudi Xue <yudi.xue@usask.ca>,
          Mathieu Bastian
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
package org.gephi.desktop.preview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This class provides some sets of properties for the preview UI.
 *
 * @author Jérémy Subtil, Yudi Xue, Mathieu Bastian
 */
public class PreviewNode extends AbstractNode implements PropertyChangeListener {

    public PreviewNode() {
        super(Children.LEAF);
        setDisplayName(NbBundle.getMessage(PreviewNode.class, "PreviewNode.displayName"));
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel model = controller.getModel();
        if (model != null) {
            PreviewProperties properties = model.getProperties();

            Map<String, Sheet.Set> sheetSets = new HashMap<String, Sheet.Set>();
            for (PreviewProperty property : properties.getProperties()) {
                String category = property.getCategory();
                Sheet.Set sheetSet = sheetSets.get(category);
                if (sheetSet == null) {
                    sheetSet = Sheet.createPropertiesSet();
                    sheetSet.setDisplayName(category);
                    sheetSet.setName(category);
                }
                Node.Property nodeProperty = null;
                PreviewProperty[] parents = properties.getParentProperties(property);
                PreviewProperty[] children = properties.getChildProperties(property);
                if (parents.length > 0) {
                    nodeProperty = new ChildPreviewPropertyWrapper(property, parents);
                } else if (children.length > 0) {
                    nodeProperty = new ParentPreviewPropertyWrapper(property, children);
                } else {
                    nodeProperty = new PreviewPropertyWrapper(property);
                }

                sheetSet.put(nodeProperty);
                sheetSets.put(category, sheetSet);
            }
            for (Sheet.Set sheetSet : sheetSets.values()) {
                sheet.put(sheetSet);
            }
        }
        return sheet;
    }

    private static class PreviewPropertyWrapper extends PropertySupport.ReadWrite {

        private final PreviewProperty property;

        public PreviewPropertyWrapper(PreviewProperty previewProperty) {
            super(previewProperty.getName(), previewProperty.getType(), previewProperty.getDisplayName(), previewProperty.getDescription());
            this.property = previewProperty;
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return property.getValue();
        }

        @Override
        public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            property.setValue(t);
        }
    }

    private static class ChildPreviewPropertyWrapper extends PropertySupport.ReadWrite {

        private final PreviewProperty property;
        private final PreviewProperty[] parents;

        public ChildPreviewPropertyWrapper(PreviewProperty previewProperty, PreviewProperty[] parents) {
            super(previewProperty.getName(), previewProperty.getType(), previewProperty.getDisplayName(), previewProperty.getDescription());
            this.property = previewProperty;
            this.parents = parents;
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return property.getValue();
        }

        @Override
        public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            property.setValue(t);
        }

        @Override
        public boolean canWrite() {
            for (PreviewProperty parent : parents) {
                if (parent.getType().equals(Boolean.class) && parent.getValue().equals(Boolean.FALSE)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class ParentPreviewPropertyWrapper extends PropertySupport.ReadWrite {

        private final PreviewProperty property;
        private final PreviewProperty[] children;

        public ParentPreviewPropertyWrapper(PreviewProperty previewProperty, PreviewProperty[] children) {
            super(previewProperty.getName(), previewProperty.getType(), previewProperty.getDisplayName(), previewProperty.getDescription());
            this.property = previewProperty;
            this.children = children;
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return property.getValue();
        }

        @Override
        public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            property.setValue(t);
            for (PreviewProperty p : children) {
                propertyChange(new PropertyChangeEvent(this, p.getName(), p.getValue(), p.getValue()));
            }
        }
    }

    /**
     * default method for PropertyChangeListener, it is necessary to fire
     * property change to update propertyEditor, which will refresh at runtime
     * if a property value has been passively updated.
     *
     * @param pce a PropertyChangeEvent from a PreviewProperty object.
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
    }
}
