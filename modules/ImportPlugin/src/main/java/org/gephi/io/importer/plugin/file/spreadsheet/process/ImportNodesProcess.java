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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Eduardo Ramos
 */
public class ImportNodesProcess extends AbstractImportProcess {

    public static final String NODE_ID = "id";
    public static final String NODE_LABEL = "label";


    public ImportNodesProcess(SpreadsheetGeneralConfiguration generalConfig, SheetParser parser, ContainerLoader container, ProgressTicket progressTicket) throws IOException {
        super(generalConfig, container, progressTicket, parser);
    }

    @Override
    public boolean execute() {
        setupColumnsIndexesAndFindSpecialColumns(Arrays.asList(NODE_ID, NODE_LABEL), generalConfig.getColumnsClasses());

        Integer idColumnIndex = specialColumnsIndexMap.get(NODE_ID);
        Integer labelColumnIndex = specialColumnsIndexMap.get(NODE_LABEL);

        Progress.start(progressTicket);
        for (SheetRow row : parser) {
            if (cancel) {
                break;
            }

            if (!checkRow(row)) {
                continue;
            }

            String id = null;
            String label = null;
            if (idColumnIndex != null) {
                id = row.get(idColumnIndex);
            }
            if (labelColumnIndex != null) {
                label = row.get(labelColumnIndex);
            }

            NodeDraft node = id != null ? container.factory().newNodeDraft(id) : container.factory().newNodeDraft();

            if (label != null) {
                node.setLabel(label);
            }

            for (Map.Entry<String, Integer> columnEntry : headersIndexMap.entrySet()) {
                String column = columnEntry.getKey();
                Integer index = columnEntry.getValue();
                Class type = headersClassMap.get(column);

                if (type == null) {
                    continue;
                }

                Object value = row.get(index);
                if (value != null) {
                    value = parseValue((String) value, type, column);

                    if (value != null) {
                        node.setValue(column, value);
                    }
                }
            }

            container.addNode(node);
        }

        Progress.finish(progressTicket);

        return !cancel;
    }

    @Override
    protected void addColumn(String name, Class type) {
        container.addNodeColumn(name, type);
    }
}
