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
