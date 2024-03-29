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

package org.gephi.io.importer.impl;

import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerUnloader;

public class ColumnDraftImpl implements ColumnDraft {

    protected final int index;
    protected final String id;
    protected final Class typeClass;
    protected final boolean dynamic;
    protected String title;
    protected Object defaultValue;

    public ColumnDraftImpl(String id, int index, boolean dynamic, Class typeClass) {
        this.id = id;
        this.index = index;
        this.typeClass = typeClass;
        this.dynamic = dynamic;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Class getTypeClass() {
        return typeClass;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Object value) {
        this.defaultValue = value;
    }

    protected int getIndex() {
        return index;
    }

    @Override
    public void setDefaultValueString(String value) {
        this.defaultValue = AttributeUtils.parse(value, typeClass);
    }

    @Override
    public Class getResolvedTypeClass(ContainerUnloader container) {
        TimeRepresentation timeRepresentation = container.getTimeRepresentation();
        Class typeClassFinal = typeClass;
        //Get final dynamic type:
        if (dynamic && !TimeSet.class.isAssignableFrom(typeClassFinal) &&
            !TimeMap.class.isAssignableFrom(typeClassFinal)) {
            if (timeRepresentation.equals(TimeRepresentation.TIMESTAMP)) {
                typeClassFinal = AttributeUtils.getTimestampMapType(typeClassFinal);
            } else {
                typeClassFinal = AttributeUtils.getIntervalMapType(typeClassFinal);
            }
        }

        return typeClassFinal;
    }

    @Override
    public Object getResolvedDefaultValue(ContainerUnloader container) {
        Class resolvedTypeClass = getResolvedTypeClass(container);

        Object resolvedDefaultValue = defaultValue;
        if (resolvedDefaultValue != null && !resolvedTypeClass.isAssignableFrom(resolvedDefaultValue.getClass())) {
            try {
                resolvedDefaultValue = AttributeUtils.parse(resolvedDefaultValue.toString(), resolvedTypeClass);
            } catch (Exception e) {
                //Failed to parse
            }
        }

        return resolvedDefaultValue;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public String toString() {
        return title + " (" + typeClass.toString() + ")";
    }
}
