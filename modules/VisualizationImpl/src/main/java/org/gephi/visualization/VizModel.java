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

package org.gephi.visualization;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.text.TextModelImpl;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class VizModel extends VizReadWriteXML{

     private boolean defaultModel = false;

    public VizModel(Workspace workspace) {
        defaultValues();
        limits = VizController.getInstance().getLimits();

        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        textModel.setTextColumns(new Column[] {gm.getNodeTable().getColumn("label")},
            new Column[] {gm.getEdgeTable().getColumn("label")});
    }

    public VizModel(boolean defaultModel) {
        this.defaultModel = defaultModel;
        defaultValues();
        limits = VizController.getInstance().getLimits();
    }

    public void init() {
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, "init", null, null);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (listeners != null) {
                    for (PropertyChangeListener l : listeners) {
                        l.propertyChange(evt);
                    }
                }
            }
        });
    }

    public boolean isDefaultModel() {
        return defaultModel;
    }

    public List<PropertyChangeListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<PropertyChangeListener> listeners) {
        this.listeners = listeners;
    }

    private void defaultValues() {
        config = VizController.getInstance().getVizConfig();
        cameraPosition = Arrays.copyOf(config.getDefaultCameraPosition(), 3);
        cameraTarget = Arrays.copyOf(config.getDefaultCameraTarget(), 3);
        textModel = new TextModelImpl();
        backgroundColor = config.getDefaultBackgroundColor();
        backgroundColorComponents = backgroundColor.getRGBComponents(backgroundColorComponents);

        showEdges = config.isDefaultShowEdges();
        lightenNonSelectedAuto = config.isDefaultLightenNonSelectedAuto();
        autoSelectNeighbor = config.isDefaultAutoSelectNeighbor();
        hideNonSelectedEdges = config.isDefaultHideNonSelectedEdges();
        uniColorSelected = config.isDefaultUniColorSelected();
        edgeHasUniColor = config.isDefaultEdgeHasUniColor();
        edgeUniColor = config.getDefaultEdgeUniColor().getRGBComponents(null);
        adjustByText = config.isDefaultAdjustByText();
        edgeSelectionColor = config.isDefaultEdgeSelectionColor();
        edgeInSelectionColor = config.getDefaultEdgeInSelectedColor().getRGBComponents(null);
        edgeOutSelectionColor = config.getDefaultEdgeOutSelectedColor().getRGBComponents(null);
        edgeBothSelectionColor = config.getDefaultEdgeBothSelectedColor().getRGBComponents(null);
        edgeScale = config.getDefaultEdgeScale();
    }

    //GETTERS
    public boolean isAdjustByText() {
        return adjustByText;
    }

    public boolean isAutoSelectNeighbor() {
        return autoSelectNeighbor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public float[] getBackgroundColorComponents() {
        return backgroundColorComponents;
    }

    public float[] getCameraPosition() {
        return cameraPosition;
    }

    public float[] getCameraTarget() {
        return cameraTarget;
    }

    public boolean isShowEdges() {
        return showEdges;
    }

    public boolean isEdgeHasUniColor() {
        return edgeHasUniColor;
    }

    public float[] getEdgeUniColor() {
        return edgeUniColor;
    }

    public boolean isHideNonSelectedEdges() {
        return hideNonSelectedEdges;
    }

    public boolean isLightenNonSelectedAuto() {
        return lightenNonSelectedAuto;
    }

    public TextModelImpl getTextModel() {
        return textModel;
    }

    public boolean isUniColorSelected() {
        return uniColorSelected;
    }

    public VizConfig getConfig() {
        return config;
    }

    public boolean isEdgeSelectionColor() {
        return edgeSelectionColor;
    }

    public float[] getEdgeInSelectionColor() {
        return edgeInSelectionColor;
    }

    public float[] getEdgeOutSelectionColor() {
        return edgeOutSelectionColor;
    }

    public float[] getEdgeBothSelectionColor() {
        return edgeBothSelectionColor;
    }

    public float getEdgeScale() {
        return edgeScale;
    }

    public GraphLimits getLimits() {
        return limits;
    }

    public float getCameraDistance() {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        return drawable.getCameraVector().length();
    }

    public void setCameraDistance(float distance) {
    }

    //EVENTS
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

}
