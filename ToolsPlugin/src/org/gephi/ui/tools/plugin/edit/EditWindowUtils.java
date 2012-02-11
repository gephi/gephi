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

import java.util.EnumSet;
import org.gephi.data.attributes.api.AttributeType;

/**
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class EditWindowUtils {

    /**
     * These AttributeTypes are not supported by default by netbeans property editor. We will use attributes of these types as Strings and parse them.
     */
    public static EnumSet<AttributeType> NotSupportedTypes = EnumSet.of(
            AttributeType.BIGINTEGER,
            AttributeType.BIGDECIMAL,
            AttributeType.LIST_BIGDECIMAL,
            AttributeType.LIST_BIGINTEGER,
            AttributeType.LIST_BOOLEAN,
            AttributeType.LIST_BYTE,
            AttributeType.LIST_CHARACTER,
            AttributeType.LIST_DOUBLE,
            AttributeType.LIST_FLOAT,
            AttributeType.LIST_INTEGER,
            AttributeType.LIST_LONG,
            AttributeType.LIST_SHORT,
            AttributeType.LIST_STRING,
            AttributeType.TIME_INTERVAL,
            AttributeType.DYNAMIC_BIGDECIMAL,
            AttributeType.DYNAMIC_BIGINTEGER,
            AttributeType.DYNAMIC_BOOLEAN,
            AttributeType.DYNAMIC_BYTE,
            AttributeType.DYNAMIC_CHAR,
            AttributeType.DYNAMIC_DOUBLE,
            AttributeType.DYNAMIC_FLOAT,
            AttributeType.DYNAMIC_INT,
            AttributeType.DYNAMIC_LONG,
            AttributeType.DYNAMIC_SHORT,
            AttributeType.DYNAMIC_STRING);

    interface AttributeValueWrapper {

        public Byte getValueByte();

        public void setValueByte(Byte object);

        public Short getValueShort();

        public void setValueShort(Short object);

        public Character getValueCharacter();

        public void setValueCharacter(Character object);

        public String getValueString();

        public void setValueString(String object);

        public Double getValueDouble();

        public void setValueDouble(Double object);

        public Float getValueFloat();

        public void setValueFloat(Float object);

        public Integer getValueInteger();

        public void setValueInteger(Integer object);

        public Boolean getValueBoolean();

        public void setValueBoolean(Boolean object);

        public Long getValueLong();

        public void setValueLong(Long object);

        /**
         * **** Other types are not supported by property editors by default so they are used and parsed as Strings *****
         */
        public String getValueAsString();

        public void setValueAsString(String value);
    }
}
