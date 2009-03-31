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
import org.gephi.project.filetype.io.GephiWriter;
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

    public GephiDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

    }

    public Project load()
    {
        FileObject fileObject = getPrimaryFile();
        if(FileUtil.isArchiveFile(fileObject))
        {
            fileObject = FileUtil.getArchiveRoot(fileObject);
            fileObject = fileObject.getChildren()[0];
        }

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fileObject.getInputStream());
            GephiReader gephiReader = new GephiReader();
            return gephiReader.readAll(doc.getDocumentElement());
           
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void save(Project project)
    {
        try
        {
            GephiWriter gephiWriter = new GephiWriter();
            gephiWriter.writeAll(project);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
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
