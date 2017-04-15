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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.TimeRepresentation;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Eduardo Ramos
 */
public class SpreadsheetGeneralConfiguration {

    public enum Mode {
        NODES_TABLE(Arrays.asList(
                ImportNodesProcess.NODE_ID,
                ImportNodesProcess.NODE_LABEL
        )),
        EDGES_TABLE(Arrays.asList(
                ImportEdgesProcess.EDGE_ID,
                ImportEdgesProcess.EDGE_KIND,
                ImportEdgesProcess.EDGE_LABEL,
                ImportEdgesProcess.EDGE_SOURCE,
                ImportEdgesProcess.EDGE_TARGET,
                ImportEdgesProcess.EDGE_TYPE
        )),
        ADJACENCY_LIST,
        MATRIX;

        private final Set<String> specialColumnNames;

        private Mode() {
            this.specialColumnNames = Collections.emptySet();
        }

        private Mode(List<String> specialColumnNames) {
            this.specialColumnNames = Collections.unmodifiableSet(new HashSet<>(specialColumnNames));
        }

        public Set<String> getSpecialColumnNames() {
            return specialColumnNames;
        }

        public boolean isSpecialColumn(String column) {
            return specialColumnNames.contains(column.trim().toLowerCase());
        }
    }

    protected final Map<String, Class> columnsClasses = new LinkedHashMap<>();

    protected Mode mode = Mode.NODES_TABLE;
    protected TimeRepresentation timeRepresentation = TimeRepresentation.INTERVAL;
    protected DateTimeZone timeZone = DateTimeZone.UTC;

    public Mode getMode() {
        return mode;
    }

    public void setTable(Mode table) {
        this.mode = table;
    }

    public TimeRepresentation getTimeRepresentation() {
        return timeRepresentation;
    }

    public void setTimeRepresentation(TimeRepresentation timeRepresentation) {
        this.timeRepresentation = timeRepresentation;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Map<String, Class> getColumnsClasses() {
        return new LinkedHashMap<>(columnsClasses);
    }

    public void setColumnsClasses(Map<String, Class> columnsClasses) {
        this.columnsClasses.clear();
        if (columnsClasses != null) {
            for (String column : columnsClasses.keySet()) {
                setColumnClass(column, columnsClasses.get(column));
            }
        }
    }

    public Class getColumnClass(String column) {
        return columnsClasses.get(column);
    }

    public void setColumnClass(String column, Class clazz) {
        columnsClasses.put(column.trim(), clazz);
    }
}
