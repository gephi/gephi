/*
 Copyright 2008-2010 Gephi
 Authors : rlfnb
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
package org.gephi.io.importer.plugin.file;

import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author rlfnb
 */
public class ImporterTGF implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        LineNumberReader lineReader = ImportUtils.getTextReader(reader);
        try {
            importData(lineReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return !cancel;
    }

    private void importData(LineNumberReader reader) throws Exception {
        Progress.start(progressTicket);        //Progress

        List<String> nodes = new ArrayList<String>();
        List<String> edges = new ArrayList<String>();
        boolean isNode = true;
        for (; reader.ready();) {
            String line = reader.readLine().trim();
            if ("#".equalsIgnoreCase(line)) {
                isNode = false;
            } else {
                if (line != null && !line.isEmpty()) {
                    if (isNode) {
                        nodes.add(line);
                    } else {
                        edges.add(line);
                    }
                }
            }
        }

        Progress.switchToDeterminate(progressTicket, nodes.size() + edges.size());

        if (nodes.isEmpty()) {
            throw new Exception("Cannot import a graph without nodes!");
        }
        for (String n : nodes) {
            addNode(n.substring(0, n.indexOf(" ")), n.substring(n.indexOf(" ")));
        }
        Progress.progress(progressTicket);      //Progress
        for (String e : edges) {
            int firstSpace = e.indexOf(" ");
            int secondSpace = e.indexOf(" ", firstSpace + 1);
            String[] tempFields = e.split(" ");
            String from = tempFields[0];
            String to = tempFields[1];
            addEdge(from, to, e.substring(secondSpace));
        }
        Progress.progress(progressTicket);      //Progress
    }

    private void addNode(String id, String label) {
        NodeDraft node;
        if (!container.nodeExists(id)) {
            node = container.factory().newNodeDraft(id);
            node.setLabel(label);
            container.addNode(node);
        }
    }

    private void addEdge(String source, String target, String label) {
        NodeDraft sourceNode;
        if (!container.nodeExists(source)) {
            sourceNode = container.factory().newNodeDraft(source);
            container.addNode(sourceNode);
        } else {
            sourceNode = container.getNode(source);
        }
        NodeDraft targetNode;
        if (!container.nodeExists(target)) {
            targetNode = container.factory().newNodeDraft(target);
            container.addNode(targetNode);
        } else {
            targetNode = container.getNode(target);
        }
        if (!container.edgeExists(sourceNode.getId(), targetNode.getId())) {
            EdgeDraft edge = container.factory().newEdgeDraft();
            edge.setSource(sourceNode);
            edge.setTarget(targetNode);
            edge.setLabel(label);
            container.addEdge(edge);
        }
    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
