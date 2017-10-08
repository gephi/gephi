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
package org.gephi.io.importer.plugin.file.spreadsheet.sheets.csv;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;

/**
 *
 * @author Eduardo Ramos
 */
public class CSVSheetParser implements SheetParser {

    private final CSVParser parser;

    public CSVSheetParser(CSVParser parser) {
        this.parser = parser;
    }

    @Override
    public Map<String, Integer> getHeaderMap() {
        Map<String, Integer> map = parser.getHeaderMap();
        if (map == null) {
            return Collections.emptyMap();
        } else {
            if (map.containsKey(null)) {//Ignore columns without header
                map.remove(null);
            }
            return map;
        }
    }

    @Override
    public long getCurrentRecordNumber() {
        return parser.getRecordNumber();
    }

    @Override
    public Iterator<SheetRow> iterator() {
        return new CSVIterator();
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

    private class CSVIterator implements Iterator<SheetRow> {

        private final Iterator<CSVRecord> iterator;
        private ErrorRow errorFound = null;

        public CSVIterator() {
            iterator = parser.iterator();
        }

        @Override
        public boolean hasNext() {
            if (errorFound != null) {
                return false;
            }

            try {
                return iterator.hasNext();
            } catch (Exception e) {
                //In case of malformed CSV or bad delimiter
                errorFound = new ErrorRow(e.getMessage());
                Logger.getLogger("").severe(e.getMessage());
                return true;
            }
        }

        @Override
        public SheetRow next() {
            if (errorFound != null) {
                return errorFound;
            } else {
                return new CSVSheetRow(iterator.next());
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class ErrorRow implements SheetRow {

        private final String errorMessage;

        public ErrorRow(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public boolean isConsistent() {
            return false;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public String get(int index) {
            if (index == 0) {
                return errorMessage;
            } else {
                return null;
            }
        }
    }
}
