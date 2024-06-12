/*
 Copyright 2008-2011 Gephi
 Authors : Eduardo Ramos
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

import java.time.ZoneId;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.TimeFormat;
import org.gephi.ui.tools.plugin.edit.EditWindowUtils.AttributeValueWrapper;

/**
 * @author Eduardo Ramos
 */
public class MultipleRowsAttributeValueWrapper implements AttributeValueWrapper {

    private final Element[] rows;
    private final Column column;
    private final TimeFormat currentTimeFormat;
    private final ZoneId dateTimeZone;
    private Object value;

    public MultipleRowsAttributeValueWrapper(Element[] rows, Column column, TimeFormat currentTimeFormat,
                                             ZoneId dateTimeZone) {
        this.rows = rows;
        this.column = column;
        this.currentTimeFormat = currentTimeFormat;
        this.dateTimeZone = dateTimeZone;
        this.value = null;
    }

    private String convertToStringIfNotNull() {
        if (value != null) {
            return AttributeUtils.print(value, currentTimeFormat, dateTimeZone);
        } else {
            return null;
        }
    }

    private void setValueToAllElements(Object object) {
        this.value = object;
        for (Element row : rows) {
            row.setAttribute(column, value);
        }
    }

    @Override
    public Byte getValueByte() {
        return (Byte) value;
    }

    @Override
    public void setValueByte(Byte object) {
        setValueToAllElements(object);
    }

    @Override
    public Short getValueShort() {
        return (Short) value;
    }

    @Override
    public void setValueShort(Short object) {
        setValueToAllElements(object);
    }

    @Override
    public Character getValueCharacter() {
        return (Character) value;
    }

    @Override
    public void setValueCharacter(Character object) {
        setValueToAllElements(object);
    }

    @Override
    public String getValueString() {
        return (String) value;
    }

    @Override
    public void setValueString(String object) {
        setValueToAllElements(object);
    }

    @Override
    public Double getValueDouble() {
        return (Double) value;
    }

    @Override
    public void setValueDouble(Double object) {
        setValueToAllElements(object);
    }

    @Override
    public Float getValueFloat() {
        return (Float) value;
    }

    @Override
    public void setValueFloat(Float object) {
        setValueToAllElements(object);
    }

    @Override
    public Integer getValueInteger() {
        return (Integer) value;
    }

    @Override
    public void setValueInteger(Integer object) {
        setValueToAllElements(object);
    }

    @Override
    public Boolean getValueBoolean() {
        return (Boolean) value;
    }

    @Override
    public void setValueBoolean(Boolean object) {
        setValueToAllElements(object);
    }

    @Override
    public Long getValueLong() {
        return (Long) value;
    }

    @Override
    public void setValueLong(Long object) {
        setValueToAllElements(object);
    }

    @Override
    public String getValueAsString() {
        return convertToStringIfNotNull();
    }

    @Override
    public void setValueAsString(String value) {
        setValueToAllElements(AttributeUtils.parse(value, column.getTypeClass()));
    }
}
