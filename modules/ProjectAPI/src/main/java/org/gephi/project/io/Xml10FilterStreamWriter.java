package org.gephi.project.io;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link XMLStreamWriter} that removes characters not allowed in XML 1.0 from text, attribute values, CDATA sections
 * and comments before passing them to the underlying writer. It is the save counterpart of {@link Xml10FilterReader}.
 * <p>
 * Names, metadata and persistence providers' data can contain such characters (e.g. control characters coming from
 * imported data), which a strict writer such as Woodstox rejects and a lenient one writes as malformed XML.
 */
class Xml10FilterStreamWriter implements XMLStreamWriter {

    private final XMLStreamWriter writer;

    Xml10FilterStreamWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     * Returns the text without the characters not allowed in XML 1.0, or the same instance when there are none.
     */
    static String stripInvalidChars(String text) {
        if (text == null) {
            return null;
        }
        int length = text.length();
        StringBuilder sb = null;
        int i = 0;
        while (i < length) {
            int codePoint = text.codePointAt(i);
            if (XMLChar.isValid(codePoint)) {
                if (sb != null) {
                    sb.appendCodePoint(codePoint);
                }
            } else if (sb == null) {
                sb = new StringBuilder(length);
                sb.append(text, 0, i);
            }
            i += Character.charCount(codePoint);
        }
        return sb != null ? sb.toString() : text;
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        writer.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writer.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writer.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writer.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writer.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writer.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        writer.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        writer.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        writer.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writer.writeAttribute(localName, stripInvalidChars(value));
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
        throws XMLStreamException {
        writer.writeAttribute(prefix, namespaceURI, localName, stripInvalidChars(value));
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writer.writeAttribute(namespaceURI, localName, stripInvalidChars(value));
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        writer.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        writer.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        writer.writeComment(stripInvalidChars(data));
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writer.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        writer.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        writer.writeCData(stripInvalidChars(data));
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        writer.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        writer.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        writer.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writer.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        writer.writeCharacters(stripInvalidChars(text));
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        int end = start + len;
        int i = start;
        while (i < end) {
            int codePoint = Character.codePointAt(text, i, end);
            if (!XMLChar.isValid(codePoint)) {
                writer.writeCharacters(stripInvalidChars(new String(text, start, len)));
                return;
            }
            i += Character.charCount(codePoint);
        }
        writer.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return writer.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        writer.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        writer.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return writer.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) {
        return writer.getProperty(name);
    }
}
