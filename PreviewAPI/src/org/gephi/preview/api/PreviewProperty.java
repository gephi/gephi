/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.api;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewProperty {

    //Constants global
    public static final String DIRECTED = "directed";
    public static final String BACKGROUND_COLOR = "background-color";
    public static final String VISIBILITY_RATIO = "visibility-ratio";
    //Constants nodes
    public static final String NODE_BORDER_WIDTH = "node.border.width";
    public static final String NODE_BORDER_COLOR = "node.border.color";
    //Constants edges
    public static final String SHOW_EDGES = "edge.show";
    public static final String EDGE_THICKNESS = "edge.thickness";
    public static final String EDGE_CURVED = "edge.curved";
    public static final String EDGE_COLOR = "edge.color";
    public static final String EDGE_RESCALE_WEIGHT = "edge.rescale-weight";
    //Constants arrows
    public static final String ARROW_SIZE = "arrow.size";
    public static final String ARROW_RADIUS = "arrow.radius";
    //Constants node labels
    public static final String SHOW_NODE_LABELS = "node.label.show";
    public static final String NODE_LABEL_FONT = "node.label.font";
    public static final String NODE_LABEL_PROPORTIONAL_SIZE = "node.label.proportinalSize";
    public static final String NODE_LABEL_COLOR = "node.label.color";
    public static final String NODE_LABEL_SHORTEN = "node.label.shorten";
    public static final String NODE_LABEL_MAX_CHAR = "node.label.max-char";
    public static final String NODE_LABEL_OUTLINE_SIZE = "node.label.outline.size";
    public static final String NODE_LABEL_OUTLINE_TRANSPARENCY = "node.label.outline.transparency";
    public static final String NODE_LABEL_OUTLINE_COLOR = "node.label.outline.color";
    //Constants UI helps
    public static final String MOVING = "canvas.moving";
    //Variables
    protected final String name;
    protected final String displayName;
    protected final String description;
    protected final Object source;
    protected final String category;
    protected final Class type;
    protected Object value;
    protected String[] dependencies = new String[0];

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

    public static PreviewProperty createProperty(Object source, String name) {
        return new PreviewProperty(source, name, Object.class, name, "", "");
    }

    public static PreviewProperty createProperty(Object source, String name, Class type) {
        return new PreviewProperty(source, name, type, name, "", "");
    }

    public static PreviewProperty createProperty(Object source, String name, Class type, String displayName, String description, String category) {
        return new PreviewProperty(source, name, type, displayName, description, category);
    }

    public static PreviewProperty createProperty(Object source, String name, Class type, String displayName, String description, String category, String... dependantProperties) {
        return new PreviewProperty(source, name, type, displayName, description, category, dependantProperties);
    }

    public <T> T getValue() {
        return (T) value;
    }

    public PreviewProperty setValue(Object value) {
        this.value = value;
        return this;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Object getSource() {
        return source;
    }

    /**
     * Return the category of the property
     */
    public String getCategory() {
        return category;
    }
}