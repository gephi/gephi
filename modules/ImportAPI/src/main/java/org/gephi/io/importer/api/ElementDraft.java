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
package org.gephi.io.importer.api;

import java.awt.Color;

/**
 *
 * @author mbastian
 */
public interface ElementDraft {

    public String getId();

    public Object getValue(String key);

    public Object getValue(String key, double timestamp);

    public double[] getTimestamps(String key);

    public String getLabel();

    public Color getColor();

    public boolean isLabelVisible();

    public float getLabelSize();

    public Color getLabelColor();

    public void setValue(String key, Object value);

    public void setValue(String key, Object value, double timestamp);

    public void setValue(String key, Object value, String dateTime);

    public void parseAndSetValue(String key, String value);

    public void parseAndSetValue(String key, String value, double timestamp);

    public void parseAndSetValue(String key, String value, String dateTime);

    public void setLabel(String label);

    public void setColor(Color color);

    public void setColor(String r, String g, String b);

    public void setColor(float r, float g, float b);

    public void setColor(int r, int g, int b);

    public void setColor(String color);

    public void setLabelVisible(boolean labelVisible);

    public void setLabelSize(float size);

    public void setLabelColor(Color color);

    public void setLabelColor(String r, String g, String b);

    public void setLabelColor(float r, float g, float b);

    public void setLabelColor(int r, int g, int b);

    public void setLabelColor(String color);

    public void addTimestamp(double timestamp);

    public void addTimestamp(String dateTime);

    public double[] getTimestamps();
}
