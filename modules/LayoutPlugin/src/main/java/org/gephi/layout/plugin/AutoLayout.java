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
package org.gephi.layout.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;

/**
 * Class to build layout scenario that runs for a certain duration. Multiple
 * layout can be chained and their duration ratio set. Moreover layout
 * property can be mananaged automatically and set in advance.
 * <p>
 * <b>Example:</b>
 * <p>
 * This will execute ForceAtlas for the first 80%, and LabelAdjust for remaining 20%
 * <pre>
 * AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
 * ForceAtlasLayout forceAtlasLayout = new ForceAtlasLayout(null);
 * AutoLayout.DynamicProperty gravity = AutoLayout.createDynamicProperty("Gravity", new Double[]{80., 400.0}, new float[]{0f, 1f}, AutoLayout.Interpolation.LINEAR);
 * AutoLayout.DynamicProperty speed = AutoLayout.createDynamicProperty("Speed", new Double[]{1.2, 0.3}, new float[]{0f, 1f}, AutoLayout.Interpolation.LINEAR);
 * AutoLayout.DynamicProperty repulsion = AutoLayout.createDynamicProperty("Repulsion strength", new Double[]{3000.0, 6000.}, new float[]{0f, 1f}, AutoLayout.Interpolation.LINEAR);
 * AutoLayout.DynamicProperty freeze = AutoLayout.createDynamicProperty("Autostab Strength", new Double(100.0), 0f);
 * autoLayout.addLayout(forceAtlasLayout, 0.8f, new AutoLayout.DynamicProperty[]{gravity, speed, repulsion, freeze});
 *
 * //LabelAdjust
 * LabelAdjust labelAdjust = new LabelAdjust(null);
 * AutoLayout.DynamicProperty speed2 = AutoLayout.createDynamicProperty("Speed", new Double[]{0.5, 0.2}, new float[]{0f, 1f}, AutoLayout.Interpolation.LINEAR);
 * autoLayout.addLayout(labelAdjust, 0.2f, new AutoLayout.DynamicProperty[]{speed2});
 * </pre>
 * Work in Progress
 *
 * @author Mathieu Bastian
 */
public class AutoLayout {

    private final float duration;
    private final List<LayoutScenario> layouts;
    private GraphModel graphModel;
    //Flags
    private long startTime = 0;
    private long lastExecutionTime;
    private float currentRatio;
    private int innerIteration;
    private float innerStart;
    private float innerRatio;
    private LayoutScenario currentLayout;
    private boolean cancel;

    public AutoLayout(long duration, TimeUnit timeUnit) {
        this.duration = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
        this.layouts = new ArrayList<>();
    }

    public void addLayout(Layout layout, float ratio) {
        layouts.add(new LayoutScenario(layout, ratio));
    }

    public void addLayout(Layout layout, float ratio, DynamicProperty[] properties) {
        for (int i = 0; i < properties.length; i++) {
            AbstractDynamicProperty property = (AbstractDynamicProperty) properties[i];
            for (LayoutProperty lp : layout.getProperties()) {
                if (lp.getCanonicalName().equalsIgnoreCase(property.getCanonicalName()) ) {
                    property.setProperty(lp.getProperty());
                    break;
                }
            }
            if (property.getProperty() == null) {
                throw new IllegalArgumentException(property.getCanonicalName() + " property cannot be found in layout");
            }
        }
        layouts.add(new LayoutScenario(layout, ratio, properties));
    }

    public void execute() {
        //System.out.println("execute");
        cancel = false;
        verifiy();
        LayoutScenario layout;
        while (!cancel && (layout = setLayout()) != null) {
            setProperties();
            layout.layout.goAlgo();
        }
        //System.out.println("finished");
    }

    public void cancel() {
        cancel = true;
    }

    private void setProperties() {
        //String log = currentLayout.layout.toString() + ": ";
        for (int i = 0; i < currentLayout.properties.length; i++) {
            DynamicProperty d = currentLayout.properties[i];
            Object val = d.getValue(innerRatio);
            if (val != null) {
                try {
                    if (val != d.getProperty().getValue()) {
                        //log += d.getProperty().getDisplayName() + "=" + val+"   ";
                        d.getProperty().setValue(val);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        //System.out.println(log);
    }

    private LayoutScenario setLayout() {
        long elapsedTime = getElapsedTime();
        long diff = elapsedTime - lastExecutionTime;
        //System.out.println(diff);
        lastExecutionTime = elapsedTime;
        currentRatio = elapsedTime / duration;
        float ratio = currentRatio + (diff / duration);   //Don't start a layout that will overcome ratio
        if (ratio >= 1f) {
            currentLayout = null;
            return currentLayout;
        }
        LayoutScenario layout = null;
        float sumRatio = 0;
        float sum = 0;
        for (int i = 0; i < layouts.size(); i++) {
            LayoutScenario l = layouts.get(i);
            if (sum <= ratio) {
                layout = l;
                sumRatio = sum;
            }
            sum += l.ratio;
        }
        if (currentLayout != layout) {
            innerStart = currentRatio;
            innerIteration = 0;
            layout.layout.setGraphModel(graphModel);
            layout.layout.resetPropertiesValues();
            layout.layout.initAlgo();
        } else {
            innerIteration++;
        }
        currentLayout = layout;

        float start = innerStart;
        float end = sumRatio + layout.ratio;
        float averageIteration = innerIteration == 0 ? 0 : (currentRatio - start) / innerIteration;
        int totalIteration = averageIteration == 0 ? 0 : (int) ((end - start) / averageIteration) - 1;
        innerRatio = totalIteration == 0 ? 0 : innerIteration / (float) totalIteration;

        return currentLayout;
    }

    private long getElapsedTime() {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    private void verifiy() {
        float sum = 0;
        layouts.stream().map(l -> l.ratio).reduce(sum, (accumulator, _item) -> accumulator += _item);
        if (sum != 1) {
            throw new RuntimeException("Ratio sum is not 1");
        }
    }

    public static DynamicProperty createDynamicProperty(String propertyName, Object value, float ratio) {
        return new SingleDynamicProperty(propertyName, value, ratio);
    }

    public static DynamicProperty createDynamicProperty(String propertyName, Object[] value, float[] ratio) {
        return new MultiDynamicProperty(propertyName, value, ratio);
    }

    public static DynamicProperty createDynamicProperty(String propertyName, Number[] value, float[] ratio, Interpolation interpolation) {
        return new InterpolateDynamicProperty(propertyName, value, ratio, interpolation);
    }

    public static interface DynamicProperty {

        public Object getValue(float ratio);

        public Property getProperty();

        public String getCanonicalName();
    }

    public enum Interpolation {

        LINEAR, LOG
    }

    private static abstract class AbstractDynamicProperty implements DynamicProperty {

        private final String propertyCanonicalName;
        protected Property property;

        public AbstractDynamicProperty(String propertyName) {
            this.propertyCanonicalName = propertyName;
        }

        @Override
        public Property getProperty() {
            return property;
        }

        void setProperty(Property property) {
            this.property = property;
        }

        @Override
        public String getCanonicalName() {
            return propertyCanonicalName;
        }
    }

    private static class SingleDynamicProperty extends AbstractDynamicProperty {

        private final Object value;
        private final float threshold;

        SingleDynamicProperty(String propertyName, Object value, float ratio) {
            super(propertyName);
            this.value = value;
            this.threshold = ratio;
        }

        @Override
        public Object getValue(float ratio) {
            try {
                if (ratio >= threshold) {
                    return value;
                }
                return property.getValue();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }
    }

    private static class MultiDynamicProperty extends AbstractDynamicProperty {

        private final Object[] value;
        private final float[] thresholds;
        private int currentIndex = 0;

        MultiDynamicProperty(String propertyName, Object[] value, float[] ratio) {
            super(propertyName);
            this.value = value;
            this.thresholds = ratio;
            if (value.length != ratio.length) {
                throw new IllegalArgumentException("Value and ratio arrays must have same length");
            }
        }

        @Override
        public Object getValue(float ratio) {
            while (currentIndex < thresholds.length && thresholds[currentIndex] < ratio) {
                currentIndex++;
            }
            return value[currentIndex];
        }
    }

    private static class InterpolateDynamicProperty extends AbstractDynamicProperty {

        private final Number[] value;
        private final float[] thresholds;
        private final Interpolation interpolation;
        private int currentIndex = 0;

        InterpolateDynamicProperty(String propertyName, Number[] value, float[] ratio, Interpolation interpolation) {
            super(propertyName);
            this.value = value;
            this.thresholds = ratio;
            this.interpolation = interpolation;
            if (value.length != ratio.length) {
                throw new IllegalArgumentException("Value and ratio arrays must have same length");
            }
        }

        @Override
        public Object getValue(float ratio) {
            while (currentIndex < thresholds.length && thresholds[currentIndex] < ratio) {
                currentIndex++;
            }
            if (currentIndex > 0) {
                float r = 1 / (thresholds[currentIndex] - thresholds[currentIndex - 1]);
                ratio = ((ratio - thresholds[currentIndex - 1]) * r);
                return value[currentIndex - 1].doubleValue() + (value[currentIndex].doubleValue() - value[currentIndex - 1].doubleValue()) * ratio;
            }
            return value[currentIndex];
        }
    }

    private static class LayoutScenario {

        private final Layout layout;
        private final float ratio;
        private final DynamicProperty[] properties;

        public LayoutScenario(Layout layout, float ratio, DynamicProperty[] properties) {
            this.layout = layout;
            this.ratio = ratio;
            this.properties = properties;
        }

        public LayoutScenario(Layout layout, float ratio) {
            this(layout, ratio, new DynamicProperty[0]);
        }
    }
}
