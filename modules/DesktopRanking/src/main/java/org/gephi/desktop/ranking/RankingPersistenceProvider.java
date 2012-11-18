/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.desktop.ranking;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.Transformer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class RankingPersistenceProvider implements WorkspacePersistenceProvider {

    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        RankingUIModel rankingUIModel = workspace.getLookup().lookup(RankingUIModel.class);
        if (rankingUIModel != null) {
            try {
                writeXML(rankingUIModel, writer);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) {
        RankingUIController ruic = Lookup.getDefault().lookup(RankingUIController.class);
        RankingUIModel ruiModel = ruic.getModel(workspace);
        try {
            readXML(ruiModel, reader);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getIdentifier() {
        return "rankinguimodel";
    }
    private static String TRANSFORMERS = "transformers";
    private static String ELEMENT_TYPE = "element_type";
    private static String TRANSFORMER = "transformer";
    private static String TRANSFORMER_CURRENT = "currenttransformer";
    private static String RANKING_CURRENT = "ranking_current";
    private static String ELEMENT_TYPE_CURRENT = "current_element_type";

    //Model
    public void writeXML(RankingUIModel model, XMLStreamWriter writer) throws XMLStreamException {
        //Model
        writer.writeStartElement(getIdentifier());
        
        //Element type
        writer.writeAttribute(ELEMENT_TYPE_CURRENT, model.currentElementType);

        //Transformers
        writer.writeStartElement(TRANSFORMERS);
        for (Entry<String, LinkedHashMap<String, Transformer>> entry : model.transformers.entrySet()) {
            for (Entry<String, Transformer> t : entry.getValue().entrySet()) {
                Transformer transformer = t.getValue();
                if (transformer instanceof Serializable) {
                    writer.writeStartElement(TRANSFORMER);
                    writer.writeAttribute("name", t.getKey());
                    writer.writeAttribute(ELEMENT_TYPE, entry.getKey());
                    if (model.currentTransformer.get(entry.getKey()).equals(transformer)) {
                        writer.writeAttribute(TRANSFORMER_CURRENT, "true");
                    }

                    //Serialize transformer
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    XMLEncoder xmlEncoder = new XMLEncoder(stream);
                    xmlEncoder.writeObject(transformer);
                    xmlEncoder.close();

                    writer.writeAttribute("data", stream.toString());

                    writer.writeEndElement();
                }
            }
        }
        writer.writeEndElement();

        //Rankings
        for (Entry<String, Ranking> r : model.currentRanking.entrySet()) {
            writer.writeStartElement(RANKING_CURRENT);
            writer.writeAttribute(ELEMENT_TYPE, r.getKey());
            writer.writeAttribute("name", r.getValue().getName());
            writer.writeEndElement();
        }
        
        writer.writeEndElement();
    }

    public void readXML(RankingUIModel model, XMLStreamReader reader) throws XMLStreamException {
        if (reader.getAttributeValue(null, ELEMENT_TYPE_CURRENT) != null) {
            //Element type
            model.currentElementType = reader.getAttributeValue(null, ELEMENT_TYPE_CURRENT);

            //Transformers
            boolean end = false;
            while (reader.hasNext() && !end) {
                Integer eventType = reader.next();
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String name = reader.getLocalName();
                    if (TRANSFORMER.equalsIgnoreCase(name)) {
                        String elmtType = reader.getAttributeValue(null, ELEMENT_TYPE);
                        String transformerName = reader.getAttributeValue(null, "name");
                        boolean current = false;
                        if (reader.getAttributeValue(null, TRANSFORMER_CURRENT) != null) {
                            current = true;
                        }
                        LinkedHashMap<String, Transformer> transMap = model.transformers.get(elmtType);
                        Transformer t = transMap.get(transformerName);
                        if (t != null && t instanceof Serializable) {

                            //Unserialize transformer
                            String valueXML = reader.getAttributeValue(null, "data");
                            XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(valueXML.getBytes()));
                            t = (Transformer) xmlDecoder.readObject();
                            transMap.put(transformerName, t);

                            if (current) {
                                model.currentTransformer.put(elmtType, t);
                            }
                        }
                    } else if (RANKING_CURRENT.equalsIgnoreCase(name)) {
                        String elmtType = reader.getAttributeValue(null, ELEMENT_TYPE);
                        String rankingName = reader.getAttributeValue(null, "name");
                        for (Ranking r : model.getRankings(elmtType)) {
                            if (r.getName().equals(rankingName)) {
                                model.currentRanking.put(elmtType, r);
                                break;
                            }
                        }
                    }
                } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                    if (getIdentifier().equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                }
            }
        }
    }
}
