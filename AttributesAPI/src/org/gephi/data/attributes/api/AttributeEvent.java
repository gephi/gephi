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
package org.gephi.data.attributes.api;

/**
 * Attribute event interface, that {@link AttributeListener } receives when the
 * attribute model or any attribute row is modified.
 * <p>
 * <ul>
 * <li><b>ADD_COLUMN:</b> One or several columns have been created, look at
 * {@link AttributeEventData#getAddedColumns() } to get data.</li>
 * <li><b>REMOVE_COLUMN:</b> One or several columns have been removed, look at
 * {@link AttributeEventData#getRemovedColumns() } to get data.</li>
 * <li><b>SET_VALUE:</b> A value has been set in a row, look at*
 * {@link AttributeEventData#getTouchedValues()} to get new values and
 * {@link AttributeEventData#getTouchedObjects() } to get objects where value
 * has been modified.</li>
 * </ul>
 *
 * @author Mathieu Bastian
 */
public interface AttributeEvent {

    /**
     * Attribute model events.
     * <ul>
     * <li><b>ADD_COLUMN:</b> One or several columns have been created, look at
     * {@link AttributeEventData#getAddedColumns() } to get data.</li>
     * <li><b>REMOVE_COLUMN:</b> One or several columns have been removed, look at
     * {@link AttributeEventData#getRemovedColumns() } to get data.</li>
     * <li><b>SET_VALUE:</b> A value has been set in a row, look at*
     * {@link AttributeEventData#getTouchedValues()} to get new values and
     * {@link AttributeEventData#getTouchedObjects() } to get objects where value
     * has been modified.</li>
     * </ul>
     */
    public enum EventType {

        ADD_COLUMN, REMOVE_COLUMN, SET_VALUE
    };

    public EventType getEventType();

    public AttributeTable getSource();

    public AttributeEventData getData();

    /**
     * Returns <code>true</code> if this event is one of these in parameters.
     * @param type  the event types that are to be compared with this event
     * @return      <code>true</code> if this event is <code>type</code>,
     *              <code>false</code> otherwise
     */
    public boolean is(EventType... type);
}
