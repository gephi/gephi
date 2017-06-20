/*
Copyright 2008-2016 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2016 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2016 Gephi Consortium.
 */
package org.gephi.io.importer.plugin.file.spreadsheet.process;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.plugin.file.spreadsheet.SpreadsheetUtils;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractImportProcess implements Closeable {

    protected final SpreadsheetGeneralConfiguration generalConfig;
    protected final ContainerLoader container;
    protected final Report report;
    protected final ProgressTicket progressTicket;
    protected final SheetParser parser;
    protected boolean cancel = false;

    protected final Map<String, Integer> specialColumnsIndexMap = new HashMap<>();
    protected final Map<String, Integer> headersIndexMap = new HashMap<>();
    protected final Map<String, Class> headersClassMap = new HashMap<>();

    public AbstractImportProcess(SpreadsheetGeneralConfiguration generalConfig, ContainerLoader container, ProgressTicket progressTicket, SheetParser parser) {
        this.generalConfig = generalConfig;
        this.container = container;
        this.progressTicket = progressTicket;
        this.parser = parser;

        this.report = new Report();

        container.setFillLabelWithId(false);
    }

    public abstract boolean execute();

    protected void setupColumnsIndexesAndFindSpecialColumns(List<String> specialColumnNames, Map<String, Class> columnsClasses) {
        Map<String, Integer> headerMap = parser.getHeaderMap();
        Set<String> lowerCaseHeaders = new HashSet<>();
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String headerName = entry.getKey().trim();
            int currentIndex = entry.getValue();
            boolean isSpecialColumn = false;

            //Only add columns that have a class defined by the user. This also allows to filter the input columns
            if (!columnsClasses.containsKey(headerName)) {
                continue;
            }

            //First check for repeated columns:
            if (lowerCaseHeaders.contains(headerName.toLowerCase())) {
                logError(getMessage("AbstractImportProcess.error.repeatedColumn", headerName));
                continue;
            } else {
                lowerCaseHeaders.add(headerName.toLowerCase());
            }

            //Then check for special columns:
            for (String specialColumnName : specialColumnNames) {
                if (headerName.equalsIgnoreCase(specialColumnName)) {
                    specialColumnsIndexMap.put(specialColumnName, currentIndex);

                    isSpecialColumn = true;
                    break;
                }
            }

            if (isSpecialColumn) {
                continue;
            }

            Class type = columnsClasses.get(headerName);
            headersClassMap.put(headerName, type);
            headersIndexMap.put(headerName, currentIndex);
            addColumn(headerName, type);
        }
    }

    protected Object parseValue(String value, Class type, String column) {
        try {
            return AttributeUtils.parse(value, type);
        } catch (Exception e) {
            logError(getMessage("AbstractImportProcess.error.parseError", value, type.getSimpleName(), column));
            return null;
        }
    }

    protected boolean checkRow(SheetRow row) {
        boolean consistent = row.isConsistent();
        if (!consistent) {
            logError(getMessage("AbstractImportProcess.error.inconsistentRow"));
        }

        return consistent;
    }

    protected void addEdge(String source, String target) {
        addEdge(source, target, 1);
    }

    protected void addEdge(String source, String target, float weight) {
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

        EdgeDraft edge = container.factory().newEdgeDraft();
        edge.setSource(sourceNode);
        edge.setTarget(targetNode);
        edge.setWeight(weight);
        container.addEdge(edge);
    }

    public boolean cancel() {
        return cancel = true;
    }

    protected void logInfo(String message) {
        SpreadsheetUtils.logInfo(report, message, parser);
    }

    protected void logWarning(String message) {
        SpreadsheetUtils.logWarning(report, message, parser);
    }

    protected void logError(String message) {
        SpreadsheetUtils.logError(report, message, parser);
    }

    @Override
    public void close() throws IOException {
        if (parser != null) {
            parser.close();
        }
    }

    public Report getReport() {
        return report;
    }

    protected String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
    }

    protected String getMessage(String key, Object... params) {
        return NbBundle.getMessage(getClass(), key, params);
    }

    protected abstract void addColumn(String name, Class type);
}
