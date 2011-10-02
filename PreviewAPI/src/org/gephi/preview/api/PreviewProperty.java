/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.api;

import java.beans.PropertyEditor;
import org.gephi.preview.spi.Renderer;
import org.openide.util.NbBundle;

/**
 * Define a (key, value) pair property attached to a {@link PreviewProperties}
 * instance.
 * <p>
 * Preview properties are created by renderers to let users configure how items
 * should be rendered. Each property should have a unique name and a type. Users
 * should use the static <code>createProperty()</code> methods to create instances.
 * <p>
 * Static default property names are defined in this class to help renderers to 
 * reuse external properties and have cleaner code.
 * <P>
 * Properties can be grouped by categories, which default are 
 * <code>PreviewProperty.CATEGORY_NODES</code>, <code>PreviewProperty.CATEGORY_EDGES</code>,
 * <code>PreviewProperty.CATEGORY_NODE_LABELS</code>, <code>PreviewProperty.CATEGORY_EDGE_LABELS</code>
 * and <code>PreviewProperty.CATEGORY_EDGE_ARROWS</code>.
 * 
 * @author Mathieu Bastian
 * @see Renderer#getProperties() 
 */
public class PreviewProperty {

    //Constants global
    /**
     * General <code>Boolean</code> property which indicates wheter the graph is directed
     */
    public static final String DIRECTED = "directed";
    /**
     * General <code>Color</code> property of the background color
     */
    public static final String BACKGROUND_COLOR = "background-color";
    /**
     * General <code>Float</code> property which indicates the ratio of the visible graph
     * used in preview. For instance if 0.5 only 50% of nodes items are built.
     */
    public static final String VISIBILITY_RATIO = "visibility-ratio";
    /**
     * General <code>Float</code> property in percentage (0-100) describing the 
     * margin size. For instance if the value is 4 the size of the margin is 4% of
     * the total graph width.
     */
    public static final String MARGIN = "margin";
    //Constants nodes
    /**
     * Node <code>Float</code> property defining the node border size.
     */
    public static final String NODE_BORDER_WIDTH = "node.border.width";
    /**
     * Node <code>DependantColor</code> property which defines the border color. A
     * dependant color value is either the node's color or a custom color.
     */
    public static final String NODE_BORDER_COLOR = "node.border.color";
    /**
     * Node <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String NODE_OPACITY = "node.opacity";
    //Constants edges
    /**
     * Edge <code>Boolean</code> property defining whether to show edges.
     */
    public static final String SHOW_EDGES = "edge.show";
    /**
     * Edge <code>Float</code> property for the edge's thickness
     */
    public static final String EDGE_THICKNESS = "edge.thickness";
    /**
     * Edge <code>Boolean</code> property whether to draw curved edges. A
     * <code>false</code> value means straight edges.
     */
    public static final String EDGE_CURVED = "edge.curved";
    /**
     * Edge <code>EdgeColor</code> property defining the edge color. It could be
     * the source's color, the target's color, a mixed color, the edge's original
     * color or a custom color.
     */
    public static final String EDGE_COLOR = "edge.color";
    /**
     * Edge <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String EDGE_OPACITY = "edge.opacity";
    /**
     * Edge <code>Boolean</code> property defining whether edge's weight should be
     * rescaled between fixed bounds.
     */
    public static final String EDGE_RESCALE_WEIGHT = "edge.rescale-weight";
    /**
     * Edge <code>Float</code> property defining an extra distance between the node
     * and the edge.
     */
    public static final String EDGE_RADIUS = "edge.radius";
    //Constants arrows
    /**
     * Arrow <code>Float</code> property defining the arrow size.
     */
    public static final String ARROW_SIZE = "arrow.size";
    //Constants node labels
    /**
     * Node Label <code>Boolean</code> property defining whether to show node labels.
     */
    public static final String SHOW_NODE_LABELS = "node.label.show";
    /**
     * Node Label <code>Font</code> property defining node label's font.
     */
    public static final String NODE_LABEL_FONT = "node.label.font";
    /**
     * Node Label <code>Boolean></code> property defining whether to use node's size
     * in label size calculation.
     */
    public static final String NODE_LABEL_PROPORTIONAL_SIZE = "node.label.proportinalSize";
    /**
     * Node Label <code>DependantOriginalColor</code> property defining the color label.
     * The color could either be the node's color, the label original color if it has any
     * or a custom color.
     */
    public static final String NODE_LABEL_COLOR = "node.label.color";
    /**
     * Node Label <code>Boolean</code> property defining whether the label is shortened.
     */
    public static final String NODE_LABEL_SHORTEN = "node.label.shorten";
    /**
     * Node Label <code>Integer</code> property defining the maximum number of
     * characters.
     */
    public static final String NODE_LABEL_MAX_CHAR = "node.label.max-char";
    /**
     * Node Label Outline <code>Float</code> property defining the outline size. 
     */
    public static final String NODE_LABEL_OUTLINE_SIZE = "node.label.outline.size";
    /**
     * Node Label Outline <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String NODE_LABEL_OUTLINE_OPACITY = "node.label.outline.opacity";
    /**
     * Node Label Outline <code>DependantColor</code> property defining the outline color.
     * The color can be the node's color or a custom color.
     */
    public static final String NODE_LABEL_OUTLINE_COLOR = "node.label.outline.color";
    public static final String NODE_LABEL_SHOW_BOX = "node.label.box";
    public static final String NODE_LABEL_BOX_COLOR = "node.label.box.color";
    public static final String NODE_LABEL_BOX_OPACITY = "node.label.box.opacity";
    //Constants edge labels
    /**
     * Edge Label <code>Boolean</code> property defining whether to show edge labels.
     */
    public static final String SHOW_EDGE_LABELS = "edge.label.show";
    /**
     * Edge Label <code>Font</code> property defining edge label's font.
     */
    public static final String EDGE_LABEL_FONT = "edge.label.font";
    /**
     * Edge Label <code>DependantOriginalColor</code> property defining the color label.
     * The color could either be the edge's color, the label original color if it has any
     * or a custom color.
     */
    public static final String EDGE_LABEL_COLOR = "edge.label.color";
    /**
     * Edge Label <code>Boolean</code> property defining whether the label is shortened.
     */
    public static final String EDGE_LABEL_SHORTEN = "edge.label.shorten";
    /**
     * Edge Label <code>Integer</code> property defining the maximum number of
     * characters.
     */
    public static final String EDGE_LABEL_MAX_CHAR = "edge.label.max-char";
    /**
     * Edge Label Outline <code>Float</code> property defining the outline size. 
     */
    public static final String EDGE_LABEL_OUTLINE_SIZE = "edge.label.outline.size";
    /**
     * Edge Label Outline <code>Float</code> property between 0-100 which defines the opacity.
     * 100 means opaque.
     */
    public static final String EDGE_LABEL_OUTLINE_OPACITY = "edge.label.outline.opacity";
    /**
     * Edge Label Outline <code>DependantColor</code> property defining the outline color.
     * The color can be the edge's color or a custom color.
     */
    public static final String EDGE_LABEL_OUTLINE_COLOR = "edge.label.outline.color";
    //Constants UI helps
    /**
     * General <code>Boolean</code> property set as <code>true</code> when the user
     * is panning the canvas. Helps to conditionally hide elements while moving to
     * speed things up.
     */
    public static final String MOVING = "canvas.moving";
    //Constants categories
    /**
     * Node category
     */
    public static final String CATEGORY_NODES = NbBundle.getMessage(PreviewProperty.class, "PreviewProperty.Category.Nodes");
    /**
     * Edge category
     */
    public static final String CATEGORY_EDGES = NbBundle.getMessage(PreviewProperty.class, "PreviewProperty.Category.Edges");
    /**
     * Node Label category
     */
    public static final String CATEGORY_NODE_LABELS = NbBundle.getMessage(PreviewProperty.class, "PreviewProperty.Category.NodeLabels");
    /**
     * Edge Label category
     */
    public static final String CATEGORY_EDGE_LABELS = NbBundle.getMessage(PreviewProperty.class, "PreviewProperty.Category.EdgeLabels");
    /**
     * Edge arrow category
     */
    public static final String CATEGORY_EDGE_ARROWS = NbBundle.getMessage(PreviewProperty.class, "PreviewProperty.Category.EdgeArrows");
    //Variables
    final String name;
    final String displayName;
    final String description;
    final Object source;
    final String category;
    final Class type;
    Object value;
    String[] dependencies = new String[0];

    PreviewProperty(Object source, String name, Class type, String displayName, String description, String category) {
        this.source = source;
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    PreviewProperty(Object source, String name, Class type, String displayName, String description, String category, String[] dependencies) {
        this.source = source;
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.dependencies = dependencies;
    }

    /**
     * Create a new preview property. The <code>name</code> should be unique.
     * @param source the property source, for instance the renderer
     * @param name the property's name
     * @return a new preview property
     */
    public static PreviewProperty createProperty(Object source, String name) {
        return new PreviewProperty(source, name, Object.class, name, "", "");
    }

    /**
     * Create a new preview property. The <code>name</code> should be unique. If
     * the type is different from basic types (Integer, Float, Double, String, 
     * Boolean or Color) make sure to implement a {@link PropertyEditor} and register it:
     * <pre>PropertyEditorManager.registerEditor(MyType.class, MyTypePropertyEditor.class);</pre>
     * @param source the property source, for instance the renderer
     * @param name the property's name
     * @param type the property's value type
     * @return a new preview property
     */
    public static PreviewProperty createProperty(Object source, String name, Class type) {
        return new PreviewProperty(source, name, type, name, "", "");
    }

    /**
     * Create a new preview property. The <code>name</code> should be unique. If
     * the type is different from basic types (Integer, Float, Double, String, 
     * Boolean or Color) make sure to implement a {@link PropertyEditor} and register it:
     * <pre>PropertyEditorManager.registerEditor(MyType.class, MyTypePropertyEditor.class);</pre>
     * The category can be one of the default categories:
     * <ul><li>PreviewProperty.CATEGORY_NODES</li>
     * <li>PreviewProperty.CATEGORY_EDGES</li>
     * <li>PreviewProperty.CATEGORY_NODE_LABELS</li>
     * <li>PreviewProperty.CATEGORY_EDGE_LABELS</li>
     * <li>PreviewProperty.CATEGORY_EDGE_ARROWS</li></ul>
     * @param source the property source, for instance the renderer
     * @param name the property's name
     * @param type the property's value type
     * @param displayName the property's display name
     * @param description the property's description
     * @param category the property's category
     * @return a new preview property
     */
    public static PreviewProperty createProperty(Object source, String name, Class type, String displayName, String description, String category) {
        return new PreviewProperty(source, name, type, displayName, description, category);
    }

    /**
     * Create a new preview property. The <code>name</code> should be unique. If
     * the type is different from basic types (Integer, Float, Double, String, 
     * Boolean or Color) make sure to implement a {@link PropertyEditor} and register it:
     * <pre>PropertyEditorManager.registerEditor(MyType.class, MyTypePropertyEditor.class);</pre>
     * The category can be one of the default categories:
     * <ul><li>PreviewProperty.CATEGORY_NODES</li>
     * <li>PreviewProperty.CATEGORY_EDGES</li>
     * <li>PreviewProperty.CATEGORY_NODE_LABELS</li>
     * <li>PreviewProperty.CATEGORY_EDGE_LABELS</li>
     * <li>PreviewProperty.CATEGORY_EDGE_ARROWS</li></ul>
     * The <code>dependantProperties</code> list is used to automatically disable
     * the property if the dependant property is not selected. The dependant properties
     * need to be <code>Boolean</code> type.
     * @param source the property source, for instance the renderer
     * @param name the property's name
     * @param type the property's value type
     * @param displayName the property's display name
     * @param description the property's description
     * @param category the property's category
     * @param dependantProperties a list of boolean properties this property depend on
     * @return a new preview property
     */
    public static PreviewProperty createProperty(Object source, String name, Class type, String displayName, String description, String category, String... dependantProperties) {
        return new PreviewProperty(source, name, type, displayName, description, category, dependantProperties);
    }

    /**
     * Returns the property value.
     * @param <T> the return type
     * @return the property value or <code>null</code>
     */
    public <T> T getValue() {
        return (T) value;
    }

    /**
     * Sets this property value and return it. The value can be <code>null</code>.
     * @param value the value to be set
     * @return this property instance
     */
    public PreviewProperty setValue(Object value) {
        this.value = value;
        return this;
    }

    /**
     * Returns the (unique) name of this property.
     * @return the property's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this property.
     * @return this property's type
     */
    public Class getType() {
        return type;
    }

    /**
     * Returns the display name of this property or <code>null</code> if not set.
     * @return this property's display name or <code>null</code>
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the description of this property or <code>null</code> if not set.
     * @return this property's description or <code>null</code>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the source object of this property.
     * @return this property's source object
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the category of this property or <code>null</code> if not set.
     * @return this property's category or <code>null</code>
     */
    public String getCategory() {
        return category;
    }
}