package org.gephi.project.io.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceBytesPersistenceProvider;

/**
 * Unlike {@link MockXMLPersistenceProviderFailWrite}, whose exception is caught and logged by
 * <code>GephiWriter</code>, a failure in <code>writeBytes</code> propagates and aborts the whole save. This is used to
 * simulate a save interrupted half-way through the write.
 */
public class MockBytesPersistenceProviderFailWrite implements WorkspaceBytesPersistenceProvider {

    @Override
    public String getIdentifier() {
        return "mockBytesFailWrite";
    }

    @Override
    public void writeBytes(DataOutputStream stream, Workspace workspace) {
        throw new RuntimeException("Failed to write bytes");
    }

    @Override
    public void readBytes(DataInputStream stream, Workspace workspace) {
    }
}
