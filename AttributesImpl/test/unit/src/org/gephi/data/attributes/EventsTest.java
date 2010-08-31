/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.data.attributes;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.model.IndexedAttributeModel;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class EventsTest {

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
