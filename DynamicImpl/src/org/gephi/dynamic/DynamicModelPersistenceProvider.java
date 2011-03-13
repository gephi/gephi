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
package org.gephi.dynamic;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class DynamicModelPersistenceProvider implements WorkspacePersistenceProvider {

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        DynamicModelImpl model = (DynamicModelImpl) workspace.getLookup().lookup(DynamicModel.class);
        if (model != null) {
            try {
                writeModel(writer, model);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        DynamicControllerImpl dynamicController = (DynamicControllerImpl) Lookup.getDefault().lookup(DynamicController.class);
        DynamicModelImpl dynamicModelImpl = (DynamicModelImpl) workspace.getLookup().lookup(DynamicModel.class);
        if (dynamicModelImpl == null) {
            dynamicModelImpl = new DynamicModelImpl(dynamicController, workspace);
            workspace.add(dynamicModelImpl);
        }
        try {
            readModel(reader, dynamicModelImpl);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public String getIdentifier() {
        return "dynamicmodel";
    }

    public void writeModel(XMLStreamWriter writer, DynamicModelImpl model) throws XMLStreamException {
        writer.writeStartElement("dynamicmodel");

        writer.writeStartElement("timeformat");
        if (model.getTimeFormat().equals(DynamicModel.TimeFormat.DATETIME)) {
            writer.writeAttribute("value", "datetime");
        } else if (model.getTimeFormat().equals(DynamicModel.TimeFormat.DATE)) {
            writer.writeAttribute("value", "date");
        } else {
            // default: if equals(DynamicModel.TimeFormat.DOUBLE)
            writer.writeAttribute("value", "double");
        }
        writer.writeEndElement();

        writer.writeEndElement();
    }

    public void readModel(XMLStreamReader reader, DynamicModelImpl model) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();
            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    if ("timeformat".equalsIgnoreCase(reader.getLocalName())) {
                        String val = reader.getAttributeValue(null, "value");
                        if (val.equalsIgnoreCase("date")) {
                            model.setTimeFormat(DynamicModel.TimeFormat.DATE);
                        } else if (val.equalsIgnoreCase("datetime")) {
                            model.setTimeFormat(DynamicModel.TimeFormat.DATETIME);
                        } else {
                            model.setTimeFormat(DynamicModel.TimeFormat.DOUBLE);
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("dynamicmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
        // Start & End
        /*
        if (!start.isEmpty()) {
        container.setTimeIntervalMin(start);
        }
        if (!end.isEmpty()) {
        container.setTimeIntervalMax(end);
        }
         */
    }
}
