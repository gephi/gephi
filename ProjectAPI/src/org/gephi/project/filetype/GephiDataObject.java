/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.project.filetype;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.project.api.Project;
import org.gephi.project.filetype.io.GephiFormatException;
import org.gephi.project.filetype.io.GephiReader;
import org.gephi.project.filetype.io.GephiWriter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
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
           GephiFormatException exception = new GephiFormatException(GephiReader.class, ex);
           exception.printStackTrace();
           NotifyDescriptor.Message e = new NotifyDescriptor.Message(exception.getMessage(),NotifyDescriptor.WARNING_MESSAGE);
           DialogDisplayer.getDefault().notifyLater(e);
        }
        return null;
    }

    public void save()
    {
        try
        {
            FileObject fileObject = getPrimaryFile();
            File outputFile = FileUtil.toFile(fileObject);
            File writeFile=outputFile;
            boolean useTempFile=false;
            if(writeFile.exists())
			{
                useTempFile=true;
                String tempFileName = writeFile.getName()+"_temp";
                writeFile = new File(writeFile.getParent(), tempFileName);
			}

            //Stream
            FileOutputStream outputStream = new FileOutputStream(writeFile);
            ZipOutputStream zipOut = new ZipOutputStream(outputStream);
			zipOut.setLevel(0);

            zipOut.putNextEntry(new ZipEntry("Project"));
			GephiWriter gephiWriter = new GephiWriter();

            //Write Document
            Document document = gephiWriter.writeAll(project);

            //Write file output
            Source source = new DOMSource(document);
            Result result = new StreamResult(zipOut);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);

            //Close
            zipOut.closeEntry();
			zipOut.finish();
            zipOut.close();

            //Clean and copy
            if(useTempFile)
            {
                String name = fileObject.getName();
                String ext = fileObject.getExt();
                
                //Delete original file
                fileObject.delete();

                //Rename temp file
                FileObject tempFileObject = FileUtil.toFileObject(writeFile);
                FileLock lock = tempFileObject.lock();
                tempFileObject.rename(lock, name,ext);
                lock.releaseLock();
            }

        }
        catch(Exception ex)
        {
            ex.printStackTrace();

            GephiFormatException exception = new GephiFormatException(GephiWriter.class, ex);
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(exception.getMessage(),NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        }
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
