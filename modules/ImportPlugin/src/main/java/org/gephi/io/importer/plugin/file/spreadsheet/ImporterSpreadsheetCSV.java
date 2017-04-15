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
package org.gephi.io.importer.plugin.file.spreadsheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.csv.CSVParser;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.EmptySheet;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.ErrorSheet;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheets.csv.CSVSheetParser;
import org.gephi.utils.CharsetToolkit;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Eduardo Ramos
 */
public class ImporterSpreadsheetCSV extends AbstractImporterSpreadsheet {

    protected char fieldDelimiter = ',';
    protected Charset charset = Charset.forName("UTF-8");

    @Override
    public SheetParser createParserWithoutHeaders() throws IOException {
        return createParser(false);
    }

    @Override
    public SheetParser createParser() throws IOException {
        boolean withFirstRecordAsHeader
                = generalConfig.getMode() == SpreadsheetGeneralConfiguration.Mode.NODES_TABLE
                || generalConfig.getMode() == SpreadsheetGeneralConfiguration.Mode.EDGES_TABLE;

        return createParser(withFirstRecordAsHeader);
    }

    private SheetParser createParser(boolean withFirstRecordAsHeader) throws IOException {
        try {
            CSVParser csvParser = SpreadsheetUtils.configureCSVParser(file, fieldDelimiter, charset, withFirstRecordAsHeader);
            return new CSVSheetParser(csvParser);
        } catch (Exception ex) {
            if (report != null) {
                SpreadsheetUtils.logError(report, ex.getMessage(), null);
                return EmptySheet.INSTANCE;
            } else {
                return new ErrorSheet(ex.getMessage());
            }
        }
    }

    @Override
    public void refreshAutoDetections() {
        autoDetectCharset();
        autoDetectFieldDelimiter();
        super.refreshAutoDetections();
    }

    private void autoDetectCharset() {
        //Try to auto-detect the charset:
        try {
            FileInputStream is = new FileInputStream(file);
            CharsetToolkit charsetToolkit = new CharsetToolkit(is);
            charsetToolkit.setDefaultCharset(Charset.forName("UTF-8"));
            charset = charsetToolkit.getCharset();
        } catch (Exception ex) {
        }
    }

    private void autoDetectFieldDelimiter() {
        //Very simple naive detector but should work in most cases:
        try (LineNumberReader reader = ImportUtils.getTextReader(FileUtil.toFileObject(file))) {
            String line = reader.readLine();

            //Check for typical delimiter chars in the header
            int commaCount = 0;
            int semicolonCount = 0;
            int tabCount = 0;
            int spaceCount = 0;

            boolean inQuote = false;
            for (char c : line.toCharArray()) {
                if (c == '"' || c == '\'') {
                    inQuote = !inQuote;
                }

                if (!inQuote) {
                    switch (c) {
                        case ',':
                            commaCount++;
                            break;
                        case ';':
                            semicolonCount++;
                            break;
                        case '\t':
                            tabCount++;
                            break;
                        case ' ':
                            spaceCount++;
                            break;
                    }
                }
            }

            int max = Collections.max(Arrays.asList(commaCount, semicolonCount, tabCount, spaceCount));
            if (commaCount == max) {
                fieldDelimiter = ',';
            } else if (semicolonCount == max) {
                fieldDelimiter = ';';
            } else if (tabCount == max) {
                fieldDelimiter = '\t';
            } else if (spaceCount == max) {
                fieldDelimiter = ' ';
            }
        } catch (IOException ex) {
        }
    }

    public char getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(char fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
