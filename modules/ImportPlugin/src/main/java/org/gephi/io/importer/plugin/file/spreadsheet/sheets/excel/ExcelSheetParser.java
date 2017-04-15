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
package org.gephi.io.importer.plugin.file.spreadsheet.sheets.excel;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;

/**
 *
 * @author Eduardo Ramos
 */
public class ExcelSheetParser implements SheetParser {

    private final Sheet sheet;
    private final boolean withFirstRecordAsHeader;
    private final Map<String, Integer> headerMap = new LinkedHashMap<>();
    private ExcelIterator iterator;

    private int rowsFirstIndex = Integer.MAX_VALUE;
    private int rowsLastIndex = Integer.MIN_VALUE;

    public ExcelSheetParser(Sheet sheet, boolean withFirstRecordAsHeader) {
        this.sheet = sheet;
        this.withFirstRecordAsHeader = withFirstRecordAsHeader;

        calculateRowStartAndEndIndexes();
        if (withFirstRecordAsHeader) {
            initHeaderInfo();
        }
    }

    private void calculateRowStartAndEndIndexes() {
        int rowsToScan;

        if (withFirstRecordAsHeader) {
            rowsToScan = 1;
        } else {
            rowsToScan = 25;
        }

        for (int i = sheet.getFirstRowNum(), j = 0; i < sheet.getLastRowNum() && j < rowsToScan; i++, j++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                rowsFirstIndex = Math.min(rowsFirstIndex, row.getFirstCellNum());
                rowsLastIndex = Math.max(rowsLastIndex, row.getLastCellNum() - 1);
            }
        }

        if (rowsFirstIndex == Integer.MAX_VALUE) {
            rowsFirstIndex = 0;
            rowsLastIndex = 0;
        }
    }

    private void initHeaderInfo() {
        Row firstRow = sheet.getRow(sheet.getFirstRowNum());
        int zeroBasedIndex = 0;
        for (int i = rowsFirstIndex; i <= rowsLastIndex; i++) {
            Cell cell = firstRow.getCell(i);
            String header = ExcelSheetRow.getRowCellAsString(cell, i);
            if (header != null && !header.trim().isEmpty()) {
                headerMap.put(header.trim(), zeroBasedIndex);
            }
            zeroBasedIndex++;
        }
    }

    @Override
    public Map<String, Integer> getHeaderMap() {
        return new LinkedHashMap<>(headerMap);
    }

    @Override
    public long getCurrentRecordNumber() {
        return iterator != null ? iterator.getRowNum() : 0;
    }

    @Override
    public Iterator<SheetRow> iterator() {
        return iterator = new ExcelIterator();
    }

    @Override
    public void close() throws IOException {
        iterator = null;
        sheet.getWorkbook().close();
    }

    private class ExcelIterator implements Iterator<SheetRow> {

        private final Iterator<Row> iterator;
        private Row currentRow = null;

        public ExcelIterator() {
            iterator = sheet.iterator();
            if (withFirstRecordAsHeader && iterator.hasNext()) {
                iterator.next();//Skip headers row
            }
        }

        @Override
        public boolean hasNext() {
            try {
                return iterator.hasNext();
            } catch (Exception e) {
                Logger.getLogger("").severe(e.getMessage());
                return false;//In case of malformed excel
            }
        }

        @Override
        public SheetRow next() {
            currentRow = iterator.next();
            return new ExcelSheetRow(currentRow, rowsFirstIndex, rowsLastIndex);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private long getRowNum() {
            if (currentRow != null) {
                return currentRow.getRowNum();
            } else {
                return 0;
            }
        }
    }
}
