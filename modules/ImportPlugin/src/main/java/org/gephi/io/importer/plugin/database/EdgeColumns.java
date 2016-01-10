package org.gephi.io.importer.plugin.database;

import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

class EdgeColumns {

    int findIdIndex(final ResultSetMetaData metaData, final PropertiesAssociations properties) throws SQLException {
        int result = -1;
        for (int i = 1; i <= metaData.getColumnCount(); ++i) {
            String columnLabel = metaData.getColumnLabel(i);
            PropertiesAssociations.EdgeProperties p = properties.getEdgeProperty(columnLabel);
            if (PropertiesAssociations.EdgeProperties.ID.equals(p)) {
                result = i;
                break;
            }
        }
        return result;
    }

    EdgeDraft getEdgeDraft(final ElementDraft.Factory factory, final ResultSet rs, final int idColumn) throws SQLException {
        String id = getIdValue(rs, idColumn);

        final EdgeDraft edge;
        if (id == null) {
            edge = factory.newEdgeDraft();
        } else {
            edge = factory.newEdgeDraft(id);
        }
        return edge;
    }

    private String getIdValue(final ResultSet rs, final int idColumn) throws SQLException {
        if (idColumn == -1) return null;

        return rs.getString(idColumn);
    }

}
