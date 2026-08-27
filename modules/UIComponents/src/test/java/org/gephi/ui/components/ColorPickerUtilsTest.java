package org.gephi.ui.components;

import javax.swing.text.PlainDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 * The bundled ColorPicker cannot be instantiated without a display, so the dialog itself is not
 * exercised here. What is exercised is the property the fix relies on: the hex field can only ever
 * hold up to six uppercase hexadecimal digits, which is what leaves the picker with nothing to
 * rewrite from inside the field's own document notification.
 */
public class ColorPickerUtilsTest {

    private static PlainDocument hexDocument() throws Exception {
        PlainDocument document = new PlainDocument();
        document.setDocumentFilter(new ColorPickerUtils.HexDocumentFilter());
        document.insertString(0, "000000", null);
        return document;
    }

    private static String replaceAll(PlainDocument document, String text) throws Exception {
        document.replace(0, document.getLength(), text, null);
        return document.getText(0, document.getLength());
    }

    @Test
    public void testHashPrefixedValueIsAccepted() throws Exception {
        Assert.assertEquals("00FF00", replaceAll(hexDocument(), "#00FF00"));
    }

    @Test
    public void testValueIsUpperCased() throws Exception {
        Assert.assertEquals("1A2B3C", replaceAll(hexDocument(), "1a2b3c"));
    }

    @Test
    public void testCanonicalValueIsUnchanged() throws Exception {
        Assert.assertEquals("336699", replaceAll(hexDocument(), "336699"));
    }

    @Test
    public void testSeparatorsAreDropped() throws Exception {
        Assert.assertEquals("ABCDEF", replaceAll(hexDocument(), "  #ab cd ef  "));
    }

    @Test
    public void testValueIsCappedToSixDigits() throws Exception {
        Assert.assertEquals("AABBCC", replaceAll(hexDocument(), "AABBCCDDEE"));
    }

    @Test
    public void testTypingStopsAtSixDigits() throws Exception {
        PlainDocument document = new PlainDocument();
        document.setDocumentFilter(new ColorPickerUtils.HexDocumentFilter());
        for (char c : "0a1b2c3d".toCharArray()) {
            document.insertString(document.getLength(), String.valueOf(c), null);
        }
        Assert.assertEquals("0A1B2C", document.getText(0, document.getLength()));
    }

    @Test
    public void testTypingInTheMiddleOfAFullFieldOvertypesTheTail() throws Exception {
        PlainDocument document = hexDocument();
        document.replace(0, document.getLength(), "FF0000", null);
        document.insertString(3, "9", null);
        Assert.assertEquals("FF0900", document.getText(0, document.getLength()));
    }

    @Test
    public void testTypingAtTheEndOfAFullFieldIsANoOp() throws Exception {
        PlainDocument document = hexDocument();
        document.replace(0, document.getLength(), "FF0000", null);
        document.insertString(document.getLength(), "9", null);
        Assert.assertEquals("FF0000", document.getText(0, document.getLength()));
    }

    @Test
    public void testReplacingASelectionOvertypesTheTailWhenTheResultWouldOverflow() throws Exception {
        PlainDocument document = hexDocument();
        document.replace(0, document.getLength(), "FF0000", null);
        // Selecting the middle "00" and typing "999" grows the field by one character, which
        // must come from evicting the last character rather than truncating "999" itself.
        document.replace(3, 2, "999", null);
        Assert.assertEquals("FF0999", document.getText(0, document.getLength()));
    }

    @Test
    public void testDocumentOnlyEverHoldsCanonicalHex() throws Exception {
        String[] pasted =
            {"#00FF00", "1a2b3c", "rgb(12, 34, 56)", "hello world", "", "0x112233", "#ABCDEF",
                "  #ab cd ef  ", "AABBCCDDEE", "#00ff00\n"};
        for (String text : pasted) {
            String content = replaceAll(hexDocument(), text);
            Assert.assertTrue("unexpected content '" + content + "' after pasting '" + text + "'",
                content.matches("[0-9A-F]{0,6}"));
        }
    }
}
