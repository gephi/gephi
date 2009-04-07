/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.project.filetype;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gephi.project.api.Project;
import org.gephi.project.filetype.io.GephiReader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.w3c.dom.Document;

public class GephiDataObject extends MultiDataObject {

    private Project project;

    public GephiDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

    }

    public Project load()
    {
        try
        {
            FileObject fileObject = getPrimaryFile();
            if(FileUtil.isArchiveFile(fileObject))
            {
                fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fileObject.getInputStream());

            //Project instance
            if(project==null)
                project = new Project();

            project.setDataObject(this);

            GephiReader gephiReader = new GephiReader();
            return gephiReader.readAll(doc.getDocumentElement(),project);
        }
        catch(Exception ex)
        {
           NotifyDescriptor.Message e = new NotifyDescriptor.Message(ex.getMessage(),NotifyDescriptor.WARNING_MESSAGE);
           DialogDisplayer.getDefault().notifyLater(e);
        }
        return null;
    }

    public void save()
    {

    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }



    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
