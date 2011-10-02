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
