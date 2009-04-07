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

package org.gephi.importer.controller;

import java.util.ArrayList;
import org.gephi.importer.api.FileType;
import org.gephi.importer.api.ImportController;
import org.gephi.importer.api.ImportException;
import org.gephi.importer.api.Importer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu
 */
public class DesktopImportController implements ImportController {

    private Importer[] importers;

    public DesktopImportController()
    {
        //Get importers
        importers = new Importer[0];
        importers = Lookup.getDefault().lookupAll(Importer.class).toArray(importers);

    }

    public void doImport(FileObject fileObject) throws ImportException 
    {
        Importer im = getMatchingImporter(fileObject);
        if(im==null)
            throw new ImportException(NbBundle.getMessage(getClass(), "error_no_matching_importer"));

        
    }

    private Importer getMatchingImporter(FileObject fileObject)
    {
         for(Importer im : importers)
        {
             if(im.isMatchingImporter(fileObject))
                return im;
        }
        return null;
    }


    public FileType[] getFileTypes() {
       ArrayList<FileType> list = new ArrayList<FileType>();
       for(Importer im : importers)
       {
           for(FileType ft : im.getFileTypes())
           {
               list.add(ft);
           }
       }
       return list.toArray(new FileType[0]);
    }
}
