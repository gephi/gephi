/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.appearance.api.Partition;

/**
 *
 * @author mbastian
 */
public abstract class PartitionImpl implements Partition {

    protected final Map<Object, Color> colorMap;

    protected PartitionImpl() {
        this.colorMap = new HashMap<>();
    }

    @Override
    public Color getColor(Object value) {
        return colorMap.get(value);
    }

    @Override
    public void setColor(Object value, Color color) {
        colorMap.put(value, color);
    }

    @Override
    public void setColors(Color[] colors) {
        Collection c = getSortedValues();
        if (c.size() != colors.length) {
            throw new IllegalArgumentException("The colors size must match the partition size");
        }
        int i = 0;
        for (Object o : c) {
            setColor(o, colors[i++]);
        }
    }

    protected abstract void refresh();

    @Override
    public Collection getSortedValues() {
        List values = new ArrayList(getValues());
        Collections.sort(values, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                float p1 = percentage(o1);
                float p2 = percentage(o2);
                return p1 > p2 ? -1 : p1 < p2 ? 1 : 0;
            }
        });
        return values;
    }
}
