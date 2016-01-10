package org.gephi.io.importer.plugin.database;

import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

class NodeColumns {

    int findIdIndex(final ResultSetMetaData metaData, final PropertiesAssociations properties) throws SQLException {
        int result = -1;
        for (int i = 1; i <= metaData.getColumnCount(); ++i) {
            String columnLabel = metaData.getColumnLabel(i);
            PropertiesAssociations.NodeProperties p = properties.getNodeProperty(columnLabel);
            if (PropertiesAssociations.NodeProperties.ID.equals(p)) {
                result = i;
                break;
            }
        }
        return result;
    }

    NodeDraft getNodeDraft(final ElementDraft.Factory factory, final ResultSet rs, final int idColumn) throws SQLException {
        String id = getIdValue(rs, idColumn);

        final NodeDraft node;
        if (id == null) {
            node = factory.newNodeDraft();
        } else {
            node = factory.newNodeDraft(id);
        }
        return node;
    }

    private String getIdValue(final ResultSet rs, final int idColumn) throws SQLException {
        if (idColumn == -1) return null;

        return rs.getString(idColumn);
    }

}
