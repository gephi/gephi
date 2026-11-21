package org.gephi.preview.plugin.renderers;

import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.Assert;
import org.junit.Test;

public class PDFUtilsTest {

    private static final String LIBERATION_SANS = "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";

    @Test
    public void testRemoveUnsupportedCharactersEmbeddedFont() throws IOException {
        try (PDDocument document = new PDDocument();
             InputStream is = PDDocument.class.getResourceAsStream(LIBERATION_SANS)) {
            assertUnsupportedCharactersRemoved(PDType0Font.load(document, is));
        }
    }

    @Test
    public void testRemoveUnsupportedCharactersStandardFont() throws IOException {
        assertUnsupportedCharactersRemoved(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
    }

    private static void assertUnsupportedCharactersRemoved(PDFont font) throws IOException {
        String label = "Node\u009D" + new String(Character.toChars(0x1F600)) + "A";
        Assert.assertThrows(IllegalArgumentException.class, () -> font.getStringWidth(label));
        Assert.assertEquals("NodeA", PDFUtils.removeUnsupportedCharacters(font, label));
    }
}
