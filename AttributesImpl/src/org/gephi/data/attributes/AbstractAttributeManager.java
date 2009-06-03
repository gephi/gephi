/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.attributes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.gephi.data.attributes.api.AttributeManager;
import org.gephi.data.attributes.api.AttributeType;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractAttributeManager implements AttributeManager {

    //Classes
    private ConcurrentMap<String, AbstractAttributeClass> classMap;
    private NodeAttributeClass nodeClass;
    private EdgeAttributeClass edgeClass;

    //Data API
    public AbstractAttributeManager() {
        classMap = new ConcurrentHashMap<String, AbstractAttributeClass>();
        nodeClass = new NodeAttributeClass(this);
        edgeClass = new EdgeAttributeClass(this);
        classMap.put(nodeClass.name, nodeClass);
        classMap.put(edgeClass.name, edgeClass);
    }

    public abstract Object getManagedValue(Object obj, AttributeType attributeType);

    public void clear() {
    }

    public Lookup getClassLookup(String className) {
        return classMap.get(className).getLookup();
    }

    public AbstractAttributeClass getNodeClass() {
        return nodeClass;
    }

    public AbstractAttributeClass getEdgeClass() {
        return edgeClass;
    }

    public AbstractAttributeClass getClass(String name) {
        AbstractAttributeClass attClass = classMap.get(name);
        if (attClass != null) {
            return attClass;
        }
        return null;
    }
}
