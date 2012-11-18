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

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.graph.api.Attributable;
import org.gephi.graph.api.Attributes;

/**
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class SingleRowAttributeValueWrapper implements EditWindowUtils.AttributeValueWrapper {

        private Attributes row;
        private AttributeColumn column;
        private TimeFormat currentTimeFormat;

        public SingleRowAttributeValueWrapper(Attributable row, AttributeColumn column, TimeFormat currentTimeFormat) {
            this.row = row.getAttributes();
            this.column = column;
            this.currentTimeFormat = currentTimeFormat;
        }

        private String convertToStringIfNotNull() {
            Object value = row.getValue(column.getIndex());
            if (value != null) {
                if (value instanceof DynamicType) {
                    return ((DynamicType) value).toString(currentTimeFormat == DynamicModel.TimeFormat.DOUBLE);
                } else {
                    return value.toString();
                }
            } else {
                return null;
            }
        }
        
        public Byte getValueByte() {
            return (Byte) row.getValue(column.getIndex());
        }

        public void setValueByte(Byte object) {
            row.setValue(column.getIndex(), object);
        }

        public Short getValueShort() {
            return (Short) row.getValue(column.getIndex());
        }

        public void setValueShort(Short object) {
            row.setValue(column.getIndex(), object);
        }

        public Character getValueCharacter() {
            return (Character) row.getValue(column.getIndex());
        }

        public void setValueCharacter(Character object) {
            row.setValue(column.getIndex(), object);
        }

        public String getValueString() {
            return (String) row.getValue(column.getIndex());
        }

        public void setValueString(String object) {
            row.setValue(column.getIndex(), object);
        }

        public Double getValueDouble() {
            return (Double) row.getValue(column.getIndex());
        }

        public void setValueDouble(Double object) {
            row.setValue(column.getIndex(), object);
        }

        public Float getValueFloat() {
            return (Float) row.getValue(column.getIndex());
        }

        public void setValueFloat(Float object) {
            row.setValue(column.getIndex(), object);
        }

        public Integer getValueInteger() {
            return (Integer) row.getValue(column.getIndex());
        }

        public void setValueInteger(Integer object) {
            row.setValue(column.getIndex(), object);
        }

        public Boolean getValueBoolean() {
            return (Boolean) row.getValue(column.getIndex());
        }

        public void setValueBoolean(Boolean object) {
            row.setValue(column.getIndex(), object);
        }

        public Long getValueLong() {
            return (Long) row.getValue(column.getIndex());
        }

        public void setValueLong(Long object) {
            row.setValue(column.getIndex(), object);
        }

        public String getValueAsString() {
            return convertToStringIfNotNull();
        }

        public void setValueAsString(String value) {
            row.setValue(column.getIndex(), column.getType().parse(value));
        }
    }
