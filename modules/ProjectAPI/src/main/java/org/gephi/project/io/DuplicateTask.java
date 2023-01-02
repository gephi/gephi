package org.gephi.project.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.spi.WorkspaceBytesPersistenceProvider;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

public class DuplicateTask implements LongTask {

    private final Workspace workspace;

    private boolean cancel = false;

    private ProgressTicket progressTicket;

    public DuplicateTask(Workspace workspace) {
        this.workspace = workspace;
    }

    public WorkspaceImpl run() {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, NbBundle.getMessage(DuplicateTask.class, "DuplicateTask.name"));

        try {
            WorkspaceImpl newWorkspace = duplicateWorkspace(workspace);

            Collection<WorkspacePersistenceProvider> providers = PersistenceProviderUtils.getPersistenceProviders();

            for (WorkspacePersistenceProvider provider : providers) {
                if (!cancel) {
                    if (provider instanceof WorkspaceXMLPersistenceProvider) {
                        duplicateWorkspaceModel(workspace, newWorkspace, (WorkspaceXMLPersistenceProvider) provider);
                    } else if (provider instanceof WorkspaceBytesPersistenceProvider) {
                        duplicateWorkspaceModel(workspace, newWorkspace, (WorkspaceBytesPersistenceProvider) provider);
                    }
                }
            }

            return newWorkspace;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Progress.finish(progressTicket);
        }
    }

    private void duplicateWorkspaceModel(Workspace workspace, Workspace newWorkspace,
                                         WorkspaceBytesPersistenceProvider persistenceProvider) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        persistenceProvider.writeBytes(dos, workspace);
        bos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        DataInputStream dis = new DataInputStream(bis);
        persistenceProvider.readBytes(dis, newWorkspace);
        bis.close();
    }

    private void duplicateWorkspaceModel(Workspace workspace, Workspace newWorkspace,
                                         WorkspaceXMLPersistenceProvider persistenceProvider) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = SaveTask.newXMLWriter(bos);
        GephiWriter.writeWorkspaceChildren(writer, workspace, persistenceProvider);
        writer.close();
        bos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        XMLStreamReader reader = LoadTask.newXMLReader(bis);
        GephiReader.readWorkspaceChildren(newWorkspace, reader, persistenceProvider);
        reader.close();
        bis.close();
    }

    private WorkspaceImpl duplicateWorkspace(Workspace workspace) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = SaveTask.newXMLWriter(bos);
        GephiWriter.writeWorkspace(writer, workspace);
        writer.close();
        bos.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        XMLStreamReader reader = LoadTask.newXMLReader(bis);
        WorkspaceImpl newWorkspace = GephiReader.readWorkspace(reader, (ProjectImpl) workspace.getProject());
        reader.close();
        bis.close();
        return newWorkspace;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
