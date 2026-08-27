package org.gephi.io.importer.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openide.filesystems.FileObject;

public class ImportUtilsTest {

    @Test
    public void testGetTextReaderPreservesCauseOnFailure() throws IOException {
        FileObject fileObject = Mockito.mock(FileObject.class);
        FileNotFoundException originalException = new FileNotFoundException("permission denied");
        Mockito.when(fileObject.getInputStream()).thenThrow(originalException);

        try {
            ImportUtils.getTextReader(fileObject);
            Assert.fail("Expected an IOException to be thrown");
        } catch (IOException ex) {
            Assert.assertSame("the original IOException should be preserved as the cause", originalException,
                ex.getCause());
        }
    }
}
