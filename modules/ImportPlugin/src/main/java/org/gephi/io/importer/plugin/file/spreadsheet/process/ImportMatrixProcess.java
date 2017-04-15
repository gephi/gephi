/*
Copyright 2008-2017 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2017 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2017 Gephi Consortium.
 */
package org.gephi.io.importer.plugin.file.spreadsheet.process;

import java.util.ArrayList;
import java.util.List;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Eduardo Ramos
 */
public class ImportMatrixProcess extends AbstractImportProcess {

    public ImportMatrixProcess(SpreadsheetGeneralConfiguration generalConfig, ContainerLoader container, ProgressTicket progressTicket, SheetParser parser) {
        super(generalConfig, container, progressTicket, parser);
    }

    @Override
    public boolean execute() {
        container.setFillLabelWithId(true);
        Progress.start(progressTicket);

        List<String> targetLabels = new ArrayList<>();
        List<String> sourceLabels = new ArrayList<>();

        boolean firstRow = true;
        int rowCount = 0;

        for (SheetRow row : parser) {
            if (firstRow) {
                //Start at 1, ignoring first value:
                for (int i = 1; i < row.size(); i++) {
                    String label = row.get(i);
                    targetLabels.add(label);

                    if (label == null) {
                        logError(getMessage("ImportMatrixProcess.error.missingTarget", i));
                    }
                }

                firstRow = false;
            } else {
                if (row.size() > 0) {
                    String source = row.get(0);
                    sourceLabels.add(source);

                    if (source != null) {
                        for (int i = 1; i < row.size(); i++) {
                            int labelIndex = i - 1;
                            if (labelIndex < targetLabels.size()) {
                                String value = row.get(i);
                                String target = targetLabels.get(labelIndex);

                                if (target != null) {
                                    try {
                                        if (value != null && !value.trim().equals("0")) {
                                            float weight = Float.parseFloat(value.replace(',', '.'));

                                            if (weight != 0) {
                                                addEdge(source.trim(), target.trim(), weight);
                                            }
                                        }
                                    } catch (NumberFormatException ex) {
                                        logError(getMessage("ImportMatrixProcess.error.parseWeightError", value));
                                    }
                                }
                            } else {
                                logError(getMessage("ImportMatrixProcess.error.invalidRowLength", row.size() - 1, targetLabels.size()));
                                break;
                            }
                        }
                    } else {
                        logError(getMessage("ImportMatrixProcess.error.missingSource"));
                    }
                }

                rowCount++;
            }
        }

        if (rowCount != targetLabels.size()) {
            logWarning(getMessage("ImportMatrixProcess.warning.inconsistentNumberOfLines", rowCount, targetLabels.size()));
        } else if (!sourceLabels.equals(targetLabels)) {
            logWarning(getMessage("ImportMatrixProcess.warning.inconsistentLabels"));
        }

        Progress.finish(progressTicket);
        return !cancel;
    }

    @Override
    protected void addColumn(String name, Class type) {
        //NOOP
    }

}
