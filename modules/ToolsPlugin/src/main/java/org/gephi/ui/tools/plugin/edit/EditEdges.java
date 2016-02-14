/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.ui.tools.plugin.edit;

import java.awt.Color;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.TextProperties;
import org.gephi.graph.api.TimeFormat;
import org.gephi.ui.tools.plugin.edit.EditWindowUtils.*;
import org.gephi.ui.tools.plugin.edit.EditWindowUtils.AttributeValueWrapper;
import org.joda.time.DateTimeZone;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * PropertySheet that allows to edit one or more edges. If multiple node edition
 * mode is used at first all values will be shown as blank but will change with
 * the editions and all edges will be set the values that the user inputs.
 *
 * @author Mathieu Bastian
 */
public class EditEdges extends AbstractNode {

    private PropertySet[] propertySets;
    private final Edge[] edges;
    private final boolean multipleEdges;
    private final TimeFormat currentTimeFormat;
    private final DateTimeZone dateTimeZone;

    /**
     * Single edge edition mode will always be enabled with this single node
     * constructor
     *
     * @param edge
     */
    public EditEdges(Edge edge) {
        super(Children.LEAF);
        this.edges = new Edge[]{edge};
        setName(edge.getLabel());
        multipleEdges = false;

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        currentTimeFormat = gc.getGraphModel().getTimeFormat();
        dateTimeZone = gc.getGraphModel().getTimeZone();
    }

    /**
     * If the edges array has more than one element, multiple edges edition mode
     * will be enabled.
     *
     * @param edges
     */
    public EditEdges(Edge[] edges) {
        super(Children.LEAF);
        this.edges = edges;
        multipleEdges = edges.length > 1;
        if (multipleEdges) {
            setName(NbBundle.getMessage(EditEdges.class, "EditEdges.multiple.elements"));
        } else {
            setName(edges[0].getLabel());
        }

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        currentTimeFormat = gc.getGraphModel().getTimeFormat();
        dateTimeZone = gc.getGraphModel().getTimeZone();
    }

    @Override
    public PropertySet[] getPropertySets() {
        propertySets = new PropertySet[]{prepareEdgesProperties(), prepareEdgesAttributes()};
        return propertySets;
    }

    /**
     * Prepare set of attributes of the edges.
     *
     * @return Set of these attributes
     */
    private Sheet.Set prepareEdgesAttributes() {
        try {
//            DynamicModel dm=Lookup.getDefault().lookup(DynamicController.class).getModel();
//            if(dm!=null){
//                currentTimeFormat=dm.getTimeFormat();
//            }
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            Sheet.Set set = new Sheet.Set();
            set.setName("attributes");
            if (edges.length > 1) {
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.attributes.text.multiple"));
            } else {
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.attributes.text", edges[0].getLabel()));
            }

            Edge row = edges[0];
            AttributeValueWrapper wrap;
            for (Column column : row.getAttributeColumns()) {
                if (multipleEdges) {
                    wrap = new MultipleRowsAttributeValueWrapper(edges, column, currentTimeFormat, dateTimeZone);
                } else {
                    wrap = new SingleRowAttributeValueWrapper(edges[0], column, currentTimeFormat, dateTimeZone);
                }
                Class<?> type = column.getTypeClass();
                Property p;
                PropertyEditor propEditor = PropertyEditorManager.findEditor(type);
                if (ac.canChangeColumnData(column)) {
                    //Editable column, provide "set" method:
                    if (propEditor != null && !type.isArray()) {//The type can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type, "getValue" + type.getSimpleName(), "setValue" + type.getSimpleName());
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", "setValueAsString");
                    }
                } else //Not editable column, do not provide "set" method:
                {
                    if (propEditor != null) {//The type can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type, "getValue" + type.getSimpleName(), null);
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", null);
                    }
                }
                p.setDisplayName(column.getTitle());
                p.setName(column.getId());
                set.put(p);
            }
            return set;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Prepare set of editable properties of the node(s): size, position.
     *
     * @return Set of these properties
     */
    private Sheet.Set prepareEdgesProperties() {
        try {
            if (multipleEdges) {
                Sheet.Set set = new Sheet.Set();
                set.setName("properties");
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.properties.text.multiple"));

                Property p;

                //Color:
                MultipleEdgesPropertiesWrapper edgesWrapper = new MultipleEdgesPropertiesWrapper(edges);
                p = new PropertySupport.Reflection(edgesWrapper, Color.class, "getEdgesColor", "setEdgesColor");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.color.text"));
                p.setName("color");
                set.put(p);

                //Label color:
                p = new PropertySupport.Reflection(edgesWrapper, Color.class, "getLabelsColor", "setLabelsColor");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.label.color.text"));
                p.setName("labelcolor");
                set.put(p);

                //Label size:
                p = new PropertySupport.Reflection(edgesWrapper, Float.class, "getLabelsSize", "setLabelsSize");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.label.size.text"));
                p.setName("labelsize");
                set.put(p);

                //Label visible:
                p = new PropertySupport.Reflection(edgesWrapper, Boolean.class, "getLabelsVisible", "setLabelsVisible");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.label.visible.text"));
                p.setName("labelvisible");
                set.put(p);

                return set;
            } else {
                Edge edge = edges[0];
                Sheet.Set set = new Sheet.Set();
                set.setName("properties");
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.properties.text", edge.getLabel()));

                Property p;

                //Color:
                SingleEdgePropertiesWrapper edgeWrapper = new SingleEdgePropertiesWrapper(edge);
                p = new PropertySupport.Reflection(edgeWrapper, Color.class, "getEdgeColor", "setEdgeColor");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.color.text"));
                p.setName("color");
                set.put(p);

                TextProperties textProperties = edge.getTextProperties();

                //Label size:
                p = new PropertySupport.Reflection(textProperties, Float.TYPE, "getSize", "setSize");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.label.size.text"));
                p.setName("labelsize");
                set.put(p);

                //Label color:
                p = new PropertySupport.Reflection(edgeWrapper, Color.class, "getLabelColor", "setLabelColor");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.label.color.text"));
                p.setName("labelcolor");
                set.put(p);

                //Label visible:
                p = new PropertySupport.Reflection(textProperties, Boolean.TYPE, "isVisible", "setVisible");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.label.visible.text"));
                p.setName("labelvisible");
                set.put(p);

                return set;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public class SingleEdgePropertiesWrapper {

        private final Edge edge;

        public SingleEdgePropertiesWrapper(Edge Edge) {
            this.edge = Edge;
        }

        public Color getEdgeColor() {
            if (edge.alpha() == 0) {
                return null;//Not specific color for edge
            }

            return new Color(edge.r(), edge.g(), edge.b(), edge.alpha());
        }

        public void setEdgeColor(Color c) {
            if (c != null) {
                edge.setR(c.getRed() / 255f);
                edge.setG(c.getGreen() / 255f);
                edge.setB(c.getBlue() / 255f);
                edge.setAlpha(c.getAlpha() / 255f);
            }
        }

        public Color getLabelColor() {
            TextProperties textProps = edge.getTextProperties();
            if (textProps.getAlpha() == 0) {
                return null;//Not specific color for label
            }

            return textProps.getColor();
        }

        public void setLabelColor(Color c) {
            if (c != null) {
                TextProperties textProps = edge.getTextProperties();
                textProps.setColor(c);
            }
        }
    }

    public class MultipleEdgesPropertiesWrapper {

        Edge[] edges;

        public MultipleEdgesPropertiesWrapper(Edge[] Edges) {
            this.edges = Edges;
        }
        //Methods and fields for multiple edges editing:
        private Color edgesColor = null;
        private Color labelsColor = null;
        private Float labelsSize = null;
        private Boolean labelsVisible = null;

        public Color getEdgesColor() {
            return edgesColor;
        }

        public void setEdgesColor(Color c) {
            if (c != null) {
                edgesColor = c;
                for (Edge edge : edges) {
                    edge.setR(c.getRed() / 255f);
                    edge.setG(c.getGreen() / 255f);
                    edge.setB(c.getBlue() / 255f);
                    edge.setAlpha(c.getAlpha() / 255f);
                }
            }
        }

        public Color getLabelsColor() {
            return labelsColor;
        }

        public void setLabelsColor(Color c) {
            if (c != null) {
                labelsColor = c;
                for (Edge edge : edges) {
                    TextProperties textProps = edge.getTextProperties();
                    textProps.setR(c.getRed() / 255f);
                    textProps.setG(c.getGreen() / 255f);
                    textProps.setB(c.getBlue() / 255f);
                    textProps.setAlpha(c.getAlpha() / 255f);
                }
            }
        }

        public Float getLabelsSize() {
            return labelsSize;
        }

        public void setLabelsSize(Float size) {
            labelsSize = size;
            for (Edge edge : edges) {
                TextProperties textProps = edge.getTextProperties();
                textProps.setSize(size);
            }
        }

        public Boolean getLabelsVisible() {
            return labelsVisible;
        }

        public void setLabelsVisible(Boolean visible) {
            labelsVisible = visible;
            for (Edge edge : edges) {
                TextProperties textProps = edge.getTextProperties();
                textProps.setVisible(visible);
            }
        }
    }
}
