/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla <bujacik@gmail.com>
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
package org.gephi.data.attributes;

import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public final class AttributeValueImpl implements AttributeValue {

    private final AttributeColumnImpl column;
    private final Object value;

    public AttributeValueImpl(AttributeColumnImpl column, Object value) {
        this.column = column;
        this.value = value;
    }

    public AttributeColumnImpl getColumn() {
        return column;
    }

    public Object getValue() {
        if (column.getOrigin() != AttributeOrigin.DELEGATE) {
            return value;
        } else {
            if (value == null) {
                return null;
            }

            AttributeValueDelegateProvider attributeValueDelegateProvider = column.getProvider();

            Object result;
            if (AttributeUtilsImpl.getDefault().isEdgeColumn(column)) {
                result = attributeValueDelegateProvider.getEdgeAttributeValue(value, column);
            } else if (AttributeUtilsImpl.getDefault().isNodeColumn(column)) {
                result = attributeValueDelegateProvider.getNodeAttributeValue(value, column);
            } else {
                throw new AssertionError();
            }
            
            if(result != null && result.getClass() != column.getType().getType()){
                //Try to parse to correct column type if the delegate provides a wrong type value:
                Object convertedValue = column.getType().parse(value.toString());
                if(convertedValue != null){
                    result = convertedValue;
                }
            }

            // important for Neo4j and in future also for other storing engines
            // the conversion can be necessary because of types mismatch
            // for Neo4j return type can be array of primitive type which must be
            // converted into List type
            if (result != null && result.getClass().isArray()) {
                result = ListFactory.fromArray(result);
            }

            return result;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AttributeValue) {
            if (this == obj) {
                return true;
            }
            Object thisVal = this.getValue();
            Object objVal = ((AttributeValue) obj).getValue();
            if (thisVal == null && objVal == null) {
                return true;
            }
            if (thisVal != null && objVal != null && thisVal.equals(objVal)) {
                return true;
            }
        }
        return false;
    }
}
