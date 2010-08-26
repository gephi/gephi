/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics;

import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.ui.statistics.plugin.ClusteringCoefficientUI;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
        model.addResult(clusteringCoefficientUI);

        Element e1 = model.writeXML(createDocument());
        String s1 = printXML(e1);
        System.out.println(s1);
    }

    private String printXML(org.w3c.dom.Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Document createDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            return document;
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
