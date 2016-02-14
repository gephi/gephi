/*
 Copyright 2008-2011 Gephi
 Authors : Jérémy Subtil <jeremy.subtil@gephi.org>,
 Yudi Xue <yudi.xue@usask.ca>,
 Mathieu Bastian
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
package org.gephi.desktop.preview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.SwingUtilities;
import org.gephi.preview.api.*;
import org.gephi.preview.spi.Renderer;
import org.openide.explorer.propertysheet.PropertySheet;
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

    private PropertySheet propertySheet;

    public PreviewNode(PropertySheet propertySheet) {
        super(Children.LEAF);
        this.propertySheet = propertySheet;
        setDisplayName(NbBundle.getMessage(PreviewNode.class, "PreviewNode.displayName"));
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);

        Set<Renderer> enabledRenderers = null;
        if (controller.getModel()!=null && controller.getModel().getManagedRenderers() != null) {
            enabledRenderers = new HashSet<>();
            for (ManagedRenderer mr : controller.getModel().getManagedRenderers()) {
                if (mr.isEnabled()) {
                    enabledRenderers.add(mr.getRenderer());
                }
            }
        }

        PreviewModel model = controller.getModel();
        if (model != null) {
            PreviewProperties properties = model.getProperties();

            Map<String, Sheet.Set> sheetSets = new HashMap<>();
            for (PreviewProperty property : properties.getProperties()) {
                Object source = property.getSource();
                boolean propertyEnabled = true;
                if (source instanceof Renderer) {
                    propertyEnabled = enabledRenderers == null || enabledRenderers.contains((Renderer) source);
                }

                if (propertyEnabled) {
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
            }

            //Ordered
            Sheet.Set nodeSet = sheetSets.remove(PreviewProperty.CATEGORY_NODES);
            Sheet.Set nodeLabelSet = sheetSets.remove(PreviewProperty.CATEGORY_NODE_LABELS);
            Sheet.Set edgeSet = sheetSets.remove(PreviewProperty.CATEGORY_EDGES);
            Sheet.Set arrowsSet = sheetSets.remove(PreviewProperty.CATEGORY_EDGE_ARROWS);
            Sheet.Set edgeLabelSet = sheetSets.remove(PreviewProperty.CATEGORY_EDGE_LABELS);
            if (nodeSet != null) {
                sheet.put(nodeSet);
            }
            if (nodeLabelSet != null) {
                sheet.put(nodeLabelSet);
            }
            if (edgeSet != null) {
                sheet.put(edgeSet);
            }
            if (arrowsSet != null) {
                sheet.put(arrowsSet);
            }
            if (edgeLabelSet != null) {
                sheet.put(edgeLabelSet);
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
     * default method for PropertyChangeListener, it is necessary to fire property change to update propertyEditor, which will refresh at runtime if a property value has been passively updated.
     *
     * @param pce a PropertyChangeEvent from a PreviewProperty object.
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                propertySheet.updateUI();
            }
        });
    }
}
