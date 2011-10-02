/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
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
package org.gephi.dynamic;

import java.util.TreeMap;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicModelEvent;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicIndex {

    protected final TreeMap<Double, Integer> lowMap;
    protected final TreeMap<Double, Integer> highMap;
    protected final DynamicModelImpl model;

    public DynamicIndex(DynamicModelImpl model) {
        this.model = model;
        lowMap = new TreeMap<Double, Integer>();
        highMap = new TreeMap<Double, Integer>();
    }

    public synchronized void add(Interval interval) {
        Double low = interval.getLow();
        Double high = interval.getHigh();
        boolean empty = lowMap.isEmpty() && highMap.isEmpty();
        if (!Double.isInfinite(low)) {
            if (lowMap.get(low) != null) {
                Integer counter = new Integer(lowMap.get(low) + 1);
                lowMap.put(low, counter);
            } else {
                Double min = lowMap.isEmpty() ? Double.POSITIVE_INFINITY : lowMap.firstKey();
                lowMap.put(low, 1);
                if (low < min) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, model, low));
                }
            }
        }
        if (!Double.isInfinite(high)) {
            if (highMap.get(high) != null) {
                Integer counter = new Integer(highMap.get(high) + 1);
                highMap.put(high, counter);
            } else {
                Double max = highMap.isEmpty() ? Double.NEGATIVE_INFINITY : highMap.lastKey();
                highMap.put(high, 1);
                if (high > max) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, model, high));
                }
            }
        }
        if (empty && !Double.isInfinite(low) && !Double.isInfinite(high)) {
            fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.IS_DYNAMIC_GRAPH, model, Boolean.TRUE));
        }
    }

    public synchronized void remove(Interval interval) {
        Double low = interval.getLow();
        Double high = interval.getHigh();
        if (!Double.isInfinite(low) && lowMap.get(low) != null) {
            Integer counter = new Integer(lowMap.get(low) - 1);
            if (counter == 0) {
                Double min = lowMap.firstKey();
                lowMap.remove(low);
                if (min.equals(low)) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, model, getMin()));
                }
            } else {
                lowMap.put(low, counter);
            }
        }
        if (!Double.isInfinite(high) && highMap.get(high) != null) {
            Integer counter = new Integer(highMap.get(high) - 1);
            if (counter == 0) {
                Double max = highMap.lastKey();
                highMap.remove(high);
                if (max.equals(high)) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, model, getMax()));
                }
            } else {
                highMap.put(high, counter);
            }
        }
        if (lowMap.isEmpty() && highMap.isEmpty()) {
            fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.IS_DYNAMIC_GRAPH, model, Boolean.FALSE));
        }
    }

    public synchronized void clear() {
        lowMap.clear();
        highMap.clear();
    }

    public synchronized double getMin() {
        return lowMap.isEmpty() ? (highMap.isEmpty() ? Double.NEGATIVE_INFINITY : highMap.firstKey()) : lowMap.firstKey();
    }

    public synchronized double getMax() {
        return highMap.isEmpty() ? (lowMap.isEmpty() ? Double.POSITIVE_INFINITY : lowMap.lastKey()) : highMap.lastKey();
    }

    private void fireEvent(DynamicModelEvent event) {
        if (model != null) {
            model.controller.fireModelEvent(event);
        }
    }
}
