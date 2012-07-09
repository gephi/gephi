/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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
package org.gephi.data.attributes.spi;


import org.gephi.data.attributes.api.AttributeColumn;


/**
 * <h3>General information</h3>
 * Provider for delegating attribute value. Using this interface it is possible to delegate the real
 * value of AttributeValue object to database (relational, graph, ...), index/search engine, ...
 * <br>
 * For every node/edge it is possible to add, get, set and remove every attribute value.
 *
 * <h3>Implementation details</h3>
 * As of implementation detail of AttributeValue immutability, both adding and setting the attribute value is done through the
 * set[Edge|Node]AttributeValue() method, so it is absolutely necessary to treat both cases in the method body.
 * <br>
 * Every method takes at least 2 arguments:
 * <ul>
 *     <li>delegate id
 *     <li>attribute column
 * </ul>
 * Delegate id is any type of object necessary to get the right node/edge. For instance for Neo4j it is of type Long,
 * which is directly used as Neo4j node/relationship id. For other storing engines which require indexing key and
 * value it could be crate (wrapper object) wrapping both values with proper types.
 * <br>
 * Attribute column is used usually for getting the column id which is used as property name.
 *
 * <h3>Automatic and manual type conversions</h3>
 * Important thing about the whole delegating process is type conversion. It is clear that there might exist type mismatch
 * between Gephi types and types which are supported in storing engine. During implementation it is necessary to fulfill
 * following requirements:
 * <ul>
 *     <li>get[Edge|Node]AttributeValue() method must return any of primitive types, String or array of these types. The
 *         conversion from array type into appropriate List type is done automatically using ListFactory method.
 *     <li>set[Edge|Node]AttributeValue() can accept any of Gephi supported types described in AttributeType constructors
 *         as class objects. It is up to the Provider to make appropriate conversion if this is needed, especially conversion
 *         from List types.
 * </ul>
 * 
 * <h3>Usage</h3>
 * Every AttributeColumn has direct link to its own AttributeValueDelegateProvider. This means that it is possible to
 * have more Providers in the same AttributeTable, each delegating from a subset of all columns. And because the fact
 * that every AttributeValue has link to its AttributeColumn, it has direct access to its Provider too.
 * <br>
 * Provider will be called for getting value for every AttributeValue which has column with AttributeOrigin.DELEGATE.
 * The first setValue() method call on Attributes/AttributeRow will set the delegate id and every other will change
 * data in storing engine.
 *
 * <h3>Best practises</h3>
 * Every implementing class should be implemented as singleton because of memory savings. This singleton should be
 * passed during populating AttributeTable / creating columns.
 * <br>
 * Any other necessary implementation information / resources (as concrete database instance) should be set using
 * static methods.
 * <br>
 * Any necessary convertor described in <b>Automatic and manual type conversions</b> should be implemented as static
 * inner class.
 *
 * @author Martin Škurla
 *
 * @param <T> type parameter used to restrict delegate id type
 */
public abstract class AttributeValueDelegateProvider<T> {
    /**
     * Returns the delegated node attribute value.
     *
     * @param delegateId      delegate id
     * @param attributeColumn attribute column
     * 
     * @return delegated node attribute value
     */
    public abstract Object getNodeAttributeValue(T delegateId, AttributeColumn attributeColumn);

    /**
     * Adds or sets the delegated node attribute value. It is necessary to treat both cases in the method body!
     *
     * @param delegateId      delegate id
     * @param attributeColumn attribute column
     * @param nodeValue       new/changed delegated node attribute value
     */
    public abstract void setNodeAttributeValue(T delegateId, AttributeColumn attributeColumn, Object nodeValue);

    /**
     * Deletes the delegated node attribute value.
     *
     * @param delegateId      delegate id
     * @param attributeColumn attribute column
     */
    public abstract void deleteNodeAttributeValue(T delegateId, AttributeColumn attributeColumn);

    /**
     * Returns the delegated edge attribute value.
     *
     * @param delegateId      delegate id
     * @param attributeColumn attribute column
     *
     * @return delegated edge attribute value
     */
    public abstract Object getEdgeAttributeValue(T delegateId, AttributeColumn attributeColumn);

    /**
     * Adds or sets the delegated edge attribute value. It is necessary to treat both cases in the method body!
     *
     * @param delegateId      delegate id
     * @param attributeColumn attribute column
     * @param nodeValue       new/changed delegated edge attribute value
     */
    public abstract void setEdgeAttributeValue(T delegateId, AttributeColumn attributeColumn, Object edgeValue);

    /**
     * Deletes the delegated edge attribute value.
     *
     * @param delegateId      delegate id
     * @param attributeColumn attribute column
     */
    public abstract void deleteEdgeAttributeValue(T delegateId, AttributeColumn attributeColumn);

    /**
     * Returns name of storage engine. This is used when graph uses more storage engines and where must be an
     * option how to differ them in GUI.
     *
     * @return name of storage engine
     */
    public abstract String storageEngineName();//TODO >>> add documentation for these two methods

    public abstract GraphItemDelegateFactoryProvider<T> graphItemDelegateFactoryProvider();

    
    @Override//TODO >>> add documentation about this usage for hashing, ...
    public final boolean equals(Object obj) {
        if (!(obj instanceof AttributeValueDelegateProvider))
            return false;

        return ((AttributeValueDelegateProvider) obj).storageEngineName().equals(this.storageEngineName());
    }

    @Override
    public final int hashCode() {
        return storageEngineName().hashCode();
    }
}
