package org.gephi.project.io;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class Xml10FilterStreamWriterTest {

    @Test
    public void testStripsInvalidCharsFromTextAndAttributes() throws Exception {
        StringWriter out = new StringWriter();
        XMLStreamWriter writer =
            new Xml10FilterStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(out));
        writer.writeStartElement("root");
        writer.writeAttribute("name", "a\u0001b");
        writer.writeCharacters("c\u0001d");
        writer.writeCharacters("-e\u0001f-".toCharArray(), 1, 3);
        writer.writeCData("g\u0001h");
        writer.writeEndElement();
        writer.close();

        Assert.assertEquals("<root name=\"ab\">cdef<![CDATA[gh]]></root>", out.toString());
    }

    @Test
    public void testKeepsSurrogatePairsAndStripsOtherInvalidChars() {
        String emoji = new String(Character.toChars(0x1F600));
        String input = "a" + emoji + "\uD800b\u0000\tc\uDC00\uFFFE\u001F"; // lone surrogates, U+FFFE, controls
        Assert.assertEquals("a" + emoji + "b\tc", Xml10FilterStreamWriter.stripInvalidChars(input));
    }

    @Test
    public void testPassesCleanTextThroughAndStripsOtherOverloads() throws Exception {
        XMLStreamWriter delegate = Mockito.mock(XMLStreamWriter.class);
        XMLStreamWriter writer = new Xml10FilterStreamWriter(delegate);

        String clean = "clean";
        char[] chars = "-clean-".toCharArray();
        writer.writeCharacters(clean);
        writer.writeCharacters(chars, 1, 5);
        writer.writeAttribute("uri", "attr", "x\u0001y");
        writer.writeAttribute("p", "uri", "attr", "x\u0001y");
        writer.writeComment("x\u0001y");

        Mockito.verify(delegate).writeCharacters(Mockito.same(clean));
        Mockito.verify(delegate).writeCharacters(Mockito.same(chars), Mockito.eq(1), Mockito.eq(5));
        Mockito.verify(delegate).writeAttribute("uri", "attr", "xy");
        Mockito.verify(delegate).writeAttribute("p", "uri", "attr", "xy");
        Mockito.verify(delegate).writeComment("xy");
        Mockito.verifyNoMoreInteractions(delegate);
    }
}
