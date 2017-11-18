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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.utils.CharsetToolkit;
import org.openide.util.NbBundle;

/**
 *
 * @author Eduardo Ramos
 */
public class SpreadsheetUtils {

    public static void logInfo(Report report, String message, SheetParser parser) {
        logIssue(report, new Issue(message, Issue.Level.INFO), parser);
    }

    public static void logWarning(Report report, String message, SheetParser parser) {
        logIssue(report, new Issue(message, Issue.Level.WARNING), parser);
    }

    public static void logError(Report report, String message, SheetParser parser) {
        logIssue(report, new Issue(message, Issue.Level.SEVERE), parser);
    }

    public static void logCritical(Report report, String message, SheetParser parser) {
        logIssue(report, new Issue(message, Issue.Level.CRITICAL), parser);
    }

    public static void logIssue(Report report, Issue issue, SheetParser parser) {
        if (parser != null) {
            String newMessage = "[" + NbBundle.getMessage(SpreadsheetUtils.class, "SpreadsheetUtils.recordNumber", parser.getCurrentRecordNumber()) + "] " + issue.getMessage();
            issue = new Issue(newMessage, issue.getLevel());
        }

        if (report != null) {
            report.logIssue(issue);
        } else {
            Level level;
            switch (issue.getLevel()) {
                case INFO:
                    level = Level.INFO;
                    break;
                case WARNING:
                    level = Level.WARNING;
                    break;
                case SEVERE:
                case CRITICAL:
                    level = Level.SEVERE;
                    break;
                default:
                    level = Level.FINE;
            }
            Logger.getLogger("").log(level, issue.getMessage());
        }
    }

    public static CSVParser configureCSVParser(File file, Character fieldSeparator, Charset charset, boolean withFirstRecordAsHeader) throws IOException {
        if (fieldSeparator == null) {
            fieldSeparator = ',';
        }

        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withDelimiter(fieldSeparator)
                .withEscape('\\')
                .withIgnoreEmptyLines(true)
                .withNullString("")
                .withIgnoreSurroundingSpaces(true)
                .withTrim(true);

        if (withFirstRecordAsHeader) {
            csvFormat = csvFormat
                    .withFirstRecordAsHeader()
                    .withAllowMissingColumnNames(false)
                    .withIgnoreHeaderCase(false);
        } else {
            csvFormat = csvFormat.withHeader((String[]) null).withSkipHeaderRecord(false);
        }

        boolean hasBOM = false;
        try (FileInputStream is = new FileInputStream(file)) {
            CharsetToolkit charsetToolkit = new CharsetToolkit(is);
            hasBOM = charsetToolkit.hasUTF8Bom() || charsetToolkit.hasUTF16BEBom() || charsetToolkit.hasUTF16LEBom();
        } catch (IOException e) {
            //NOOP
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader is = new InputStreamReader(fileInputStream, charset);
        if (hasBOM) {
            try {
                is.read();
            } catch (IOException e) {
                // should never happen, as a file with no content
                // but with a BOM has at least one char
            }
        }
        return new CSVParser(is, csvFormat);
    }
}
