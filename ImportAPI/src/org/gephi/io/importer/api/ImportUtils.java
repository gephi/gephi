/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.gephi.utils.CharsetToolkit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Mathieu Bastian
 */
public final class ImportUtils {

    /**
     * Returns a <code>LineNumberReader</code> for <code>fileObject</code>. The file must
     * be a text file. The charset is detected automatically.
     * @param fileObject    the file object that is to be read
     * @return              a reader for the text file
     * @throws IOException  if the file can't be found or read
     */
    public static LineNumberReader getTextReader(FileObject fileObject) throws IOException {
        try {
            return getTextReader(fileObject.getInputStream());
        } catch (IOException ex) {
            throw new IOException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_file_not_found"));
        }
    }

    /**
     * Returns a <code>LineNumberReader</code> for <code>inputStream</code>. The stream must
     * be a character stream. The charset is detected automatically.
     * @param stream    the stream that is to be read
     * @return          a reader for the character stream
     * @throws IOException if the stream can't be read
     */
    public static LineNumberReader getTextReader(InputStream stream) throws IOException {
        LineNumberReader reader;
        CharsetToolkit charsetToolkit = new CharsetToolkit(stream);
        reader = (LineNumberReader) charsetToolkit.getReader();
        return reader;
    }

    public static LineNumberReader getTextReader(Reader reader) {
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        return lineNumberReader;
    }

    public static Document getXMLDocument(InputStream stream) throws RuntimeException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            return document;
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_missing_document_instance_factory"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_file_not_found"));
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_sax"));
        } catch (IOException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_io"));
        }
    }

    public static Document getXMLDocument(Reader reader) throws RuntimeException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(reader));
            return document;
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_missing_document_instance_factory"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_file_not_found"));
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_sax"));
        } catch (IOException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_io"));
        }
    }

    public static Document getXMLDocument(FileObject fileObject) throws RuntimeException {
        try {
            InputStream stream = fileObject.getInputStream();
            return getXMLDocument(stream);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_file_not_found"));
        }
    }

    public static XMLStreamReader getXMLReader(Reader reader) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            inputFactory.setXMLReporter(new XMLReporter() {

                @Override
                public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                    throw new RuntimeException("Error:" + errorType + ", message : " + message);
                    //System.out.println("Error:" + errorType + ", message : " + message);
                }
            });
            return inputFactory.createXMLStreamReader(reader);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_io"));
        }
    }

    public static FileObject getArchivedFile(FileObject fileObject) {
        if (FileUtil.isArchiveFile(fileObject)) {
            //Unzip
            fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
        }
        return fileObject;
    }
}
