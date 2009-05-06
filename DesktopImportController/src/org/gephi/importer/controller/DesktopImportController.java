/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.importer.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.data.network.api.DhnsController;
import org.gephi.data.network.api.EdgeFactory;
import org.gephi.data.network.api.FlatImporter;
import org.gephi.data.network.api.NodeFactory;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.importer.EdgeDraftImpl;
import org.gephi.importer.NodeDraftImpl;
import org.gephi.importer.api.CustomImporter;
import org.gephi.importer.api.EdgeDraft;
import org.gephi.importer.api.FileType;
import org.gephi.importer.api.ImportController;
import org.gephi.importer.api.ImportException;
import org.gephi.importer.api.Importer;
import org.gephi.importer.api.NodeDraft;
import org.gephi.importer.api.TextImporter;
import org.gephi.importer.api.XMLImporter;
import org.gephi.importer.container.ImportContainerImpl;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopImportController implements ImportController {

    private Importer[] importers;

    public DesktopImportController() {
        //Get importers
        importers = new Importer[0];
        importers = Lookup.getDefault().lookupAll(Importer.class).toArray(importers);

    }

    public void doDynamicImport(InputStream stream) throws ImportException {
    }

    public void doImport(FileObject fileObject) throws ImportException {
        Importer im = getMatchingImporter(fileObject);
        if (im == null) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_no_matching_importer"));
        }

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = projectController.importFile();

        //Create Container
        ImportContainerImpl container = new ImportContainerImpl();

        if (im instanceof XMLImporter) {
            Document document = getDocument(fileObject);
            XMLImporter xmLImporter = (XMLImporter) im;
            xmLImporter.importData(document, container);
            finishImport(container);
        } else if (im instanceof TextImporter) {
            BufferedReader reader = getTextReader(fileObject);
            TextImporter textImporter = (TextImporter) im;
            textImporter.importData(reader, container);
            finishImport(container);
        } else if (im instanceof CustomImporter) {
        }
    }

    private void finishImport(ImportContainerImpl container) {
        container.checkNodeLabels();
        /*
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        dynamicController.appendData(container);*/
        DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
        FlatImporter flatImporter = dhnsController.getFlatImporter();

        flatImporter.initImport();

        //Nodes
        for (NodeDraft node : container.getNodes()) {
            Node n = NodeFactory.createNode();
            

            NodeDraftImpl im = (NodeDraftImpl)node;
            im.flushToNode(n);

            flatImporter.addNode(n);
        }

        //Edges
        for (EdgeDraft edge : container.getEdges()) {
            Node nodeSource = ((EdgeDraftImpl)edge).getNodeSource().getNode();
            Node nodeTarget = ((EdgeDraftImpl)edge).getNodeTarget().getNode();

            Edge e = EdgeFactory.createEdge(nodeSource, nodeTarget);

            flatImporter.addEdge(e);
        }

        flatImporter.finishImport();
        

    }



    private BufferedReader getTextReader(FileObject fileObject) throws ImportException {
        File file = FileUtil.toFile(fileObject);
        try {
            if (file == null) {
                throw new FileNotFoundException();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            return reader;
        } catch (FileNotFoundException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_file_not_found"));
        }
    }

    private Document getDocument(FileObject fileObject) throws ImportException {
        File file = FileUtil.toFile(fileObject);
        try {
            if (file == null) {
                throw new FileNotFoundException();
            }
            InputStream stream = new FileInputStream(file);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            return document;
        } catch (ParserConfigurationException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_missing_document_instance_factory"));
        } catch (FileNotFoundException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_file_not_found"));
        } catch (SAXException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_sax"));
        } catch (IOException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_io"));
        }
    }

    private Importer getMatchingImporter(FileObject fileObject) {
        for (Importer im : importers) {
            if (im.isMatchingImporter(fileObject)) {
                return im;
            }
        }
        return null;
    }

    public FileType[] getFileTypes() {
        ArrayList<FileType> list = new ArrayList<FileType>();
        for (Importer im : importers) {
            for (FileType ft : im.getFileTypes()) {
                list.add(ft);
            }
        }
        return list.toArray(new FileType[0]);
    }
}
