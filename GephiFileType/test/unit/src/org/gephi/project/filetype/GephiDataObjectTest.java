/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.project.filetype;

import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

public class GephiDataObjectTest extends TestCase {

    public GephiDataObjectTest(String testName) {
        super(testName);
    }

    public void testDataObject() throws Exception {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject template = root.getFileObject("Templates/Other/GephiTemplate.gephi");
        assertNotNull("Template file shall be found", template);

        DataObject obj = DataObject.find(template);
        assertEquals("It is our data object", GephiDataObject.class, obj.getClass());
    }
}
