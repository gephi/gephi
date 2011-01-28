/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
