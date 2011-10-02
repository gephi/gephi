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
package org.gephi.data.attributes;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.event.AttributeEventManager;
import org.gephi.data.attributes.model.IndexedAttributeModel;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class EventsTest {

    private int countEvents = 0;
    private int countElements = 0;

    @Test
    public void testEventsPerformance() {
        IndexedAttributeModel attModel = new IndexedAttributeModel();
        attModel.addAttributeListener(new AttributeListener() {

            public void attributesChanged(AttributeEvent event) {
                if(event.is(AttributeEvent.EventType.SET_VALUE)) {
                    countEvents++;
                    countElements+=event.getData().getTouchedValues().length;
                }
            }
        });

        //Add table
        AttributeTableImpl table = new AttributeTableImpl(attModel, "table");
        attModel.addTable(table);

        //Add Column
        AttributeColumnImpl col = table.addColumn("test", AttributeType.DOUBLE);

        AttributeRowImpl r1 = attModel.getFactory().newRowForTable("table", 1.0);
        AttributeRowImpl r2 = attModel.getFactory().newRowForTable("table", 1.0);

        for(int i=0;i<1000000;i++) {
            r1.setValue(col.getIndex(), Math.random());
            r2.setValue(col.getIndex(), Math.random());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Number events: "+countEvents+"  with "+countElements+" elements touched");
    }

    @Test
    public void testEvents() {
        IndexedAttributeModel attModel = new IndexedAttributeModel();
        EventCollector eventCollector = new EventCollector();
        attModel.addAttributeListener(eventCollector);

        //Add table
        AttributeTableImpl table = new AttributeTableImpl(attModel, "table");
        attModel.addTable(table);

        //Add Column
        AttributeColumnImpl col = table.addColumn("test", AttributeType.STRING);

        //Create objects
        Object o1 = new Object() {

            @Override
            public String toString() {
                return "o1";
            }
        };
        Object o2 = new Object() {

            @Override
            public String toString() {
                return "o2";
            }
        };
        AttributeRowImpl r1 = attModel.getFactory().newRowForTable("table", o1);
        AttributeRowImpl r2 = attModel.getFactory().newRowForTable("table", o2);

        //Set values
        r1.setValue(col, "value 1");
        r2.setValue(col, "value 2");

        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        //Look events
        eventCollector.print();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static class EventCollector implements AttributeListener {

        private List<AttributeEvent> events = new ArrayList<AttributeEvent>();

        public void attributesChanged(AttributeEvent event) {
            events.add(event);
        }

        public void print() {
            for (AttributeEvent e : events) {
                System.out.println("Event: " + e.getEventType() + "    source: " + e.getSource().getName());
                switch (e.getEventType()) {
                    case ADD_COLUMN:
                        for (AttributeColumn c : e.getData().getAddedColumns()) {
                            System.out.println("-- "+c.getTitle());
                        }
                        break;
                    case REMOVE_COLUMN:
                        for (AttributeColumn c : e.getData().getRemovedColumns()) {
                            System.out.println("-- "+c.getTitle());
                        }
                        break;
                    case SET_VALUE:
                        for (int i = 0; i < e.getData().getTouchedValues().length; i++) {
                            AttributeValue val = e.getData().getTouchedValues()[i];
                            Object obj = e.getData().getTouchedObjects()[i];
                            System.out.println("-- Value '" + val.getValue() + "' set for '" + obj.toString() + "' in column '" + val.getColumn().getTitle() + "'");
                        }
                        break;
                }
            }
        }
    }
}
