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

package org.gephi.statistics;

import java.io.StringReader;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.ui.statistics.plugin.ClusteringCoefficientUI;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class StatisticsPersistenceProviderTest {

    @Test
    public void testSerialization() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel attributeModel = attributeController.getModel();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();

        StatisticsModelImpl model = new StatisticsModelImpl();

        ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
        ClusteringCoefficientUI clusteringCoefficientUI = new ClusteringCoefficientUI();
        clusteringCoefficient.execute(graphModel, attributeModel);
        model.addReport(clusteringCoefficient);
        clusteringCoefficientUI.setup(clusteringCoefficient);

        GraphDensity graphDensity = new GraphDensity();
        graphDensity.execute(graphModel, attributeModel);
        model.addReport(graphDensity);

        GraphDistance graphDistance = new GraphDistance();
        graphDistance.execute(graphModel, attributeModel);
        model.addReport(graphDistance);

        try {
            StringWriter stringWriter = new StringWriter();
            XMLStreamWriter writer = createWriter(stringWriter);
            model.writeXML(writer);
            writer.close();
            String s1 = stringWriter.toString();
            StatisticsModelImpl model2 = new StatisticsModelImpl();
            StringReader stringReader = new StringReader(s1);
            XMLStreamReader reader = createReader(stringReader);
            model2.readXML(reader);
            reader.close();
            stringWriter = new StringWriter();
            writer = createWriter(stringWriter);
            model2.writeXML(writer);
            writer.close();
            String s2 = stringWriter.toString();
            assertEquals(s1, s2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private XMLStreamWriter createWriter(StringWriter stringWriter) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);

        try {
            XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stringWriter);
            xmlWriter.writeStartDocument("UTF-8", "1.0");
            return xmlWriter;
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private XMLStreamReader createReader(StringReader stringReader) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
            inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        }
        inputFactory.setXMLReporter(new XMLReporter() {

            @Override
            public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                System.out.println("Error:" + errorType + ", message : " + message);
            }
        });
        try {
            return inputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
