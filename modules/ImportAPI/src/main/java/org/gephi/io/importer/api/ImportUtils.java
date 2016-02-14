/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.io.importer.api;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.gephi.utils.CharsetToolkit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
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
     * Returns a <code>LineNumberReader</code> for <code>fileObject</code>. The
     * file must be a text file. The charset is detected automatically.
     *
     * @param fileObject the file object that is to be read
     * @return a reader for the text file
     * @throws IOException if the file can't be found or read
     */
    public static LineNumberReader getTextReader(FileObject fileObject) throws IOException {
        try {
            return getTextReader(fileObject.getInputStream());
        } catch (IOException ex) {
            throw new IOException(NbBundle.getMessage(ImportUtils.class, "ImportUtils.error_file_not_found"));
        }
    }

    public static Color parseColor(String colorString) {
        colorString = colorString.toLowerCase().replace(" ", "");

        Color cl;
        try {
            Field field = Color.class.getField(colorString);
            cl = (Color) field.get(null);
        } catch (NoSuchFieldException e) {
            cl = null; // Not defined
        } catch (SecurityException e) {
            cl = null; // Not defined
        } catch (IllegalArgumentException e) {
            cl = null; // Not defined
        } catch (IllegalAccessException e) {
            cl = null; // Not defined
        }
        if (cl == null) {
            Integer colorInt = COLORS.get(colorString);
            if (colorInt != null) {
                cl = new Color(colorInt);
            }
        }
        if (cl == null) {
            try {
                cl = Color.decode(colorString);
            } catch (NumberFormatException e) {
                cl = null;
            }
        }
        return cl;
    }

    /**
     * Returns a <code>LineNumberReader</code> for <code>inputStream</code>. The
     * stream must be a character stream. The charset is detected automatically.
     *
     * @param stream the stream that is to be read
     * @return a reader for the character stream
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
            fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
        }
        return fileObject;
    }

    public static File getBzipFile(FileObject in, File out, boolean isTar) throws IOException {

        // Stream buffer
        final int BUFF_SIZE = 8192;
        final byte[] buffer = new byte[BUFF_SIZE];

        BZip2CompressorInputStream inputStream = null;
        FileOutputStream outStream = null;

        try {
            FileInputStream is = new FileInputStream(in.getPath());
            inputStream = new BZip2CompressorInputStream(is);
            outStream = new FileOutputStream(out.getAbsolutePath());

            if (isTar) {
                // Read Tar header
                int remainingBytes = readTarHeader(inputStream);

                // Read content
                ByteBuffer bb = ByteBuffer.allocateDirect(4 * BUFF_SIZE);
                byte[] tmpCache = new byte[BUFF_SIZE];
                int nRead, nGet;
                while ((nRead = inputStream.read(tmpCache)) != -1) {
                    if (nRead == 0) {
                        continue;
                    }
                    bb.put(tmpCache);
                    bb.position(0);
                    bb.limit(nRead);
                    while (bb.hasRemaining() && remainingBytes > 0) {
                        nGet = Math.min(bb.remaining(), BUFF_SIZE);
                        nGet = Math.min(nGet, remainingBytes);
                        bb.get(buffer, 0, nGet);
                        outStream.write(buffer, 0, nGet);
                        remainingBytes -= nGet;
                    }
                    bb.clear();
                }
            } else {
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, len);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }

        return out;
    }

    public static File getGzFile(FileObject in, File out, boolean isTar) throws IOException {

        // Stream buffer
        final int BUFF_SIZE = 8192;
        final byte[] buffer = new byte[BUFF_SIZE];

        GZIPInputStream inputStream = null;
        FileOutputStream outStream = null;

        try {
            inputStream = new GZIPInputStream(new FileInputStream(in.getPath()));
            outStream = new FileOutputStream(out);

            if (isTar) {
                // Read Tar header
                int remainingBytes = readTarHeader(inputStream);

                // Read content
                ByteBuffer bb = ByteBuffer.allocateDirect(4 * BUFF_SIZE);
                byte[] tmpCache = new byte[BUFF_SIZE];
                int nRead, nGet;
                while ((nRead = inputStream.read(tmpCache)) != -1) {
                    if (nRead == 0) {
                        continue;
                    }
                    bb.put(tmpCache);
                    bb.position(0);
                    bb.limit(nRead);
                    while (bb.hasRemaining() && remainingBytes > 0) {
                        nGet = Math.min(bb.remaining(), BUFF_SIZE);
                        nGet = Math.min(nGet, remainingBytes);
                        bb.get(buffer, 0, nGet);
                        outStream.write(buffer, 0, nGet);
                        remainingBytes -= nGet;
                    }
                    bb.clear();
                }
            } else {
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, len);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }

        return out;
    }

    private static int readTarHeader(InputStream inputStream) throws IOException {
        // Tar bytes
        final int FILE_SIZE_OFFSET = 124;
        final int FILE_SIZE_LENGTH = 12;
        final int HEADER_LENGTH = 512;

        ignoreBytes(inputStream, FILE_SIZE_OFFSET);
        String fileSizeLengthOctalString = readString(inputStream, FILE_SIZE_LENGTH).trim();
        final int fileSize = Integer.parseInt(fileSizeLengthOctalString, 8);

        ignoreBytes(inputStream, HEADER_LENGTH - (FILE_SIZE_OFFSET + FILE_SIZE_LENGTH));

        return fileSize;
    }

    private static void ignoreBytes(InputStream inputStream, int numberOfBytes) throws IOException {
        for (int counter = 0; counter < numberOfBytes; counter++) {
            inputStream.read();
        }
    }

    private static String readString(InputStream inputStream, int numberOfBytes) throws IOException {
        return new String(readBytes(inputStream, numberOfBytes));
    }

    private static byte[] readBytes(InputStream inputStream, int numberOfBytes) throws IOException {
        byte[] readBytes = new byte[numberOfBytes];
        inputStream.read(readBytes);

        return readBytes;
    }

    //COLORS
    private static final Map<String, Integer> COLORS = new HashMap<>();

    static {
        COLORS.put("gray30", 0x4D4D4D);
        COLORS.put("cadetblue", 0xB0B7C6);
        COLORS.put("gray35", 0x595959);
        COLORS.put("lightorange", 0xFF6F1A);
        COLORS.put("gray75", 0xBFBFBF);
        COLORS.put("apricot", 0xFDD9B5);
        COLORS.put("canary", 0xFFFF99);
        COLORS.put("rubinered", 0xCA005D);
        COLORS.put("gray80", 0xCCCCCC);
        COLORS.put("gray20", 0x333333);
        COLORS.put("processblue", 0x4169E1);
        COLORS.put("gray25", 0x404040);
        COLORS.put("emerald", 0x50C878);
        COLORS.put("lightpurple", 0xE066FF);
        COLORS.put("forestgreen", 0x6DAE81);
        COLORS.put("maroon", 0xC8385A);
        COLORS.put("gray40", 0x666666);
        COLORS.put("seagreen", 0x9FE2BF);
        COLORS.put("thistle", 0xD8BFD8);
        COLORS.put("olivegreen", 0xBAB86C);
        COLORS.put("gray45", 0x737373);
        COLORS.put("cerulean", 0x1DACD6);
        COLORS.put("skyblue", 0x80DAEB);
        COLORS.put("violetred", 0xF75394);
        COLORS.put("lskyblue", 0x87CEFA);
        COLORS.put("gray85", 0xD9D9D9);
        COLORS.put("navyblue", 0x1974D2);
        COLORS.put("lavender", 0xFCB4D5);
        COLORS.put("lightmagenta", 0xFF00FF);
        COLORS.put("yellowgreen", 0xC5E384);
        COLORS.put("plum", 0x8E4585);
        COLORS.put("gray90", 0xE5E5E5);
        COLORS.put("gray10", 0x1A1A1A);
        COLORS.put("melon", 0xFDBCB4);
        COLORS.put("turquoise", 0x77DDE7);
        COLORS.put("midnightblue", 0x1A4876);
        COLORS.put("gray15", 0x262626);
        COLORS.put("royalpurple", 0x7851A9);
        COLORS.put("gray95", 0xF2F2F2);
        COLORS.put("brickred", 0xCB4154);
        COLORS.put("salmon", 0xFF9BAA);
        COLORS.put("rhodamine", 0xE0119D);
        COLORS.put("lfadedgreen", 0x548B54);
        COLORS.put("tan", 0xFAA76C);
        COLORS.put("rawsienna", 0xD68A59);
        COLORS.put("sepia", 0xA5694F);
        COLORS.put("lightyellow", 0xFFFFE0);
        COLORS.put("gray55", 0x8C8C8C);
        COLORS.put("bluegreen", 0x199EBD);
        COLORS.put("lightgreen", 0x90EE90);
        COLORS.put("burntorange", 0xFF7F49);
        COLORS.put("goldenrod", 0xFCD975);
        COLORS.put("blueviolet", 0x7366BD);
        COLORS.put("periwinkle", 0xC5D0E6);
        COLORS.put("aquamarine", 0x78DBE2);
        COLORS.put("redorange", 0xFF5349);
        COLORS.put("greenyellow", 0xF0E891);
        COLORS.put("mulberry", 0xAA709F);
        COLORS.put("limegreen", 0x32CD32);
        COLORS.put("darkorchid", 0xFDDB7D);
        COLORS.put("pinegreen", 0x158078);
        COLORS.put("gray60", 0x999999);
        COLORS.put("tealblue", 0x8080);
        COLORS.put("gray05", 0xD0D0D);
        COLORS.put("purple", 0x926EAE);
        COLORS.put("gray65", 0xA6A6A6);
        COLORS.put("cornflowerblue", 0x9ACEEB);
        COLORS.put("redviolet", 0xC0448F);
        COLORS.put("peach", 0xFFCFAB);
        COLORS.put("springgreen", 0xECEABE);
        COLORS.put("royalblue", 0x4169E1);
        COLORS.put("mahogany", 0xCD4A4A);
        COLORS.put("wildstrawberry", 0xFF43A4);
        COLORS.put("lightcyan", 0xE0FFFF);
        COLORS.put("orangered", 0xFF5349);
        COLORS.put("orchid", 0xE6A8D7);
        COLORS.put("dandelion", 0xFDDB6D);
        COLORS.put("violet", 0x926EAE);
        COLORS.put("fuchsia", 0xC364C5);
        COLORS.put("gray70", 0xB3B3B3);
        COLORS.put("brown", 0xB4674D);
        COLORS.put("carnationpink", 0xFFAACC);
        COLORS.put("bittersweet", 0xFD7C6E);
        COLORS.put("junglegreen", 0x3BB08F);
        COLORS.put("yelloworange", 0xFFB653);
        COLORS.put("navajowhite", 0xFFDEAD);
        COLORS.put("deeppink4", 0x8B0A50);
        COLORS.put("deeppink3", 0xCD1076);
        COLORS.put("deeppink2", 0xEE1289);
        COLORS.put("saddlebrown", 0x8B4513);
        COLORS.put("deeppink1", 0xFF1493);
        COLORS.put("burlywood3", 0xCDAA7D);
        COLORS.put("burlywood4", 0x8B7355);
        COLORS.put("lightblue", 0xADD8E6);
        COLORS.put("burlywood1", 0xFFD39B);
        COLORS.put("burlywood2", 0xEEC591);
        COLORS.put("sienna3", 0xCD6839);
        COLORS.put("peachpuff3", 0xCDAF95);
        COLORS.put("sienna2", 0xEE7942);
        COLORS.put("peachpuff2", 0xEECBAD);
        COLORS.put("sienna4", 0x8B4726);
        COLORS.put("peachpuff4", 0x8B7765);
        COLORS.put("peachpuff1", 0xFFDAB9);
        COLORS.put("orangered4", 0x8B2500);
        COLORS.put("purple4", 0x551A8B);
        COLORS.put("orangered3", 0xCD3700);
        COLORS.put("purple3", 0x7D26CD);
        COLORS.put("purple2", 0x912CEE);
        COLORS.put("purple1", 0x9B30FF);
        COLORS.put("lavenderblush", 0xFFF0F5);
        COLORS.put("palegreen2", 0x90EE90);
        COLORS.put("orangered1", 0xFF4500);
        COLORS.put("palegreen1", 0x9AFF9A);
        COLORS.put("orangered2", 0xEE4000);
        COLORS.put("palegreen4", 0x548B54);
        COLORS.put("sgiindigo2", 0x218868);
        COLORS.put("palegreen3", 0x7CCD7C);
        COLORS.put("mediumslateblue", 0x7B68EE);
        COLORS.put("linen", 0xFAF0E6);
        COLORS.put("chartreuse3", 0x66CD00);
        COLORS.put("mediumorchid", 0xBA55D3);
        COLORS.put("chartreuse2", 0x76EE00);
        COLORS.put("chartreuse4", 0x458B00);
        COLORS.put("chartreuse1", 0x7FFF00);
        COLORS.put("salmon2", 0xEE8262);
        COLORS.put("salmon3", 0xCD7054);
        COLORS.put("salmon4", 0x8B4C39);
        COLORS.put("salmon1", 0xFF8C69);
        COLORS.put("dodgerblue", 0x1E90FF);
        COLORS.put("grey", 0xBEBEBE);
        COLORS.put("dodgerblue3", 0x1874CD);
        COLORS.put("dodgerblue4", 0x104E8B);
        COLORS.put("papayawhip", 0xFFEFD5);
        COLORS.put("dodgerblue1", 0x1E90FF);
        COLORS.put("dodgerblue2", 0x1C86EE);
        COLORS.put("slategrey", 0x708090);
        COLORS.put("paleturquoise1", 0xBBFFFF);
        COLORS.put("darkgoldenrod1", 0xFFB90F);
        COLORS.put("paleturquoise2", 0xAEEEEE);
        COLORS.put("aquamarine1", 0x7FFFD4);
        COLORS.put("chocolate", 0xD2691E);
        COLORS.put("darkgoldenrod3", 0xCD950C);
        COLORS.put("darkgoldenrod2", 0xEEAD0E);
        COLORS.put("aquamarine3", 0x66CDAA);
        COLORS.put("coral2", 0xEE6A50);
        COLORS.put("aquamarine2", 0x76EEC6);
        COLORS.put("coral1", 0xFF7256);
        COLORS.put("darkgoldenrod4", 0x8B6508);
        COLORS.put("paleturquoise3", 0x96CDCD);
        COLORS.put("coral4", 0x8B3E2F);
        COLORS.put("paleturquoise4", 0x668B8B);
        COLORS.put("aquamarine4", 0x458B74);
        COLORS.put("coral3", 0xCD5B45);
        COLORS.put("mediumseagreen", 0x3CB371);
        COLORS.put("slategray1", 0xC6E2FF);
        COLORS.put("gray0", 0x000000);
        COLORS.put("slategray3", 0x9FB6CD);
        COLORS.put("mistyrose2", 0xEED5D2);
        COLORS.put("gray2", 0x050505);
        COLORS.put("slategray2", 0xB9D3EE);
        COLORS.put("mistyrose1", 0xFFE4E1);
        COLORS.put("gray1", 0x030303);
        COLORS.put("oldlace", 0xFDF5E6);
        COLORS.put("mistyrose4", 0x8B7D7B);
        COLORS.put("slategray4", 0x6C7B8B);
        COLORS.put("mistyrose3", 0xCDB7B5);
        COLORS.put("paleturquoise", 0xAFEEEE);
        COLORS.put("gray22", 0x383838);
        COLORS.put("gray21", 0x363636);
        COLORS.put("gray26", 0x424242);
        COLORS.put("powderblue", 0xB0E0E6);
        COLORS.put("gray24", 0x3D3D3D);
        COLORS.put("gray23", 0x3B3B3B);
        COLORS.put("gray7", 0x121212);
        COLORS.put("gray8", 0x141414);
        COLORS.put("gray29", 0x4A4A4A);
        COLORS.put("palevioletred", 0xDB7093);
        COLORS.put("gray9", 0x171717);
        COLORS.put("gray28", 0x474747);
        COLORS.put("gray27", 0x454545);
        COLORS.put("gray3", 0x080808);
        COLORS.put("gray4", 0x0A0A0A);
        COLORS.put("gray5", 0x0D0D0D);
        COLORS.put("azure", 0xF0FFFF);
        COLORS.put("gray6", 0x0F0F0F);
        COLORS.put("lightskyblue2", 0xA4D3EE);
        COLORS.put("lightblue4", 0x68838B);
        COLORS.put("lightskyblue1", 0xB0E2FF);
        COLORS.put("lightskyblue4", 0x607B8B);
        COLORS.put("cyan1", 0x00FFFF);
        COLORS.put("lightskyblue3", 0x8DB6CD);
        COLORS.put("cyan2", 0x00EEEE);
        COLORS.put("lightblue1", 0xBFEFFF);
        COLORS.put("lightblue2", 0xB2DFEE);
        COLORS.put("lightblue3", 0x9AC0CD);
        COLORS.put("gray11", 0x1C1C1C);
        COLORS.put("gray13", 0x212121);
        COLORS.put("gray12", 0x1F1F1F);
        COLORS.put("gray14", 0x242424);
        COLORS.put("gray17", 0x2B2B2B);
        COLORS.put("violetred1", 0xFF3E96);
        COLORS.put("gray16", 0x292929);
        COLORS.put("gold4", 0x8B7500);
        COLORS.put("gray19", 0x303030);
        COLORS.put("gray18", 0x2E2E2E);
        COLORS.put("gold2", 0xEEC900);
        COLORS.put("cyan4", 0x008B8B);
        COLORS.put("gold3", 0xCDAD00);
        COLORS.put("cyan3", 0x00CDCD);
        COLORS.put("gold1", 0xFFD700);
        COLORS.put("darkolivegreen", 0x556B2F);
        COLORS.put("violetred3", 0xCD3278);
        COLORS.put("tan3", 0xCD853F);
        COLORS.put("violetred2", 0xEE3A8C);
        COLORS.put("tan4", 0x8B5A2B);
        COLORS.put("darkgoldenrod", 0xB8860B);
        COLORS.put("tan1", 0xFFA54F);
        COLORS.put("violetred4", 0x8B2252);
        COLORS.put("tan2", 0xEE9A49);
        COLORS.put("palegreen", 0x98FB98);
        COLORS.put("darkorange4", 0x8B4500);
        COLORS.put("darkseagreen", 0x8FBC8F);
        COLORS.put("springgreen4", 0x008B45);
        COLORS.put("springgreen3", 0x00CD66);
        COLORS.put("springgreen2", 0x00EE76);
        COLORS.put("springgreen1", 0x00FF7F);
        COLORS.put("ivory2", 0xEEEEE0);
        COLORS.put("ivory1", 0xFFFFF0);
        COLORS.put("ivory4", 0x8B8B83);
        COLORS.put("ivory3", 0xCDCDC1);
        COLORS.put("beige", 0xF5F5DC);
        COLORS.put("darkorange3", 0xCD6600);
        COLORS.put("darkorange2", 0xEE7600);
        COLORS.put("darkorange1", 0xFF7F00);
        COLORS.put("mediumorchid3", 0xB452CD);
        COLORS.put("mediumorchid2", 0xD15FEE);
        COLORS.put("mediumorchid1", 0xE066FF);
        COLORS.put("mediumorchid4", 0x7A378B);
        COLORS.put("khaki", 0xF0E68C);
        COLORS.put("slategray", 0x708090);
        COLORS.put("mintcream", 0xF5FFFA);
        COLORS.put("mistyrose", 0xFFE4E1);
        COLORS.put("tomato", 0xFF6347);
        COLORS.put("moccasin", 0xFFE4B5);
        COLORS.put("royalblue3", 0x3A5FCD);
        COLORS.put("blue1", 0x0000FF);
        COLORS.put("royalblue4", 0x27408B);
        COLORS.put("royalblue1", 0x4876FF);
        COLORS.put("royalblue2", 0x436EEE);
        COLORS.put("blue4", 0x00008B);
        COLORS.put("lightslategrey", 0x778899);
        COLORS.put("blue3", 0x0000CD);
        COLORS.put("blue2", 0x0000EE);
        COLORS.put("indigo", 0x4B0082);
        COLORS.put("darkviolet", 0x9400D3);
        COLORS.put("darkred", 0x8B0000);
        COLORS.put("lightcyan1", 0xE0FFFF);
        COLORS.put("lightcyan2", 0xD1EEEE);
        COLORS.put("lightcyan3", 0xB4CDCD);
        COLORS.put("lightcyan4", 0x7A8B8B);
        COLORS.put("darkmagenta", 0x8B008B);
        COLORS.put("darkcyan", 0x008B8B);
        COLORS.put("grey89", 0xE3E3E3);
        COLORS.put("lightgoldenrodyellow", 0xFAFAD2);
        COLORS.put("grey91", 0xE8E8E8);
        COLORS.put("darkseagreen1", 0xC1FFC1);
        COLORS.put("grey90", 0xE5E5E5);
        COLORS.put("peachpuff", 0xFFDAB9);
        COLORS.put("mediumvioletred", 0xC71585);
        COLORS.put("grey99", 0xFCFCFC);
        COLORS.put("grey98", 0xFAFAFA);
        COLORS.put("grey97", 0xF7F7F7);
        COLORS.put("grey96", 0xF5F5F5);
        COLORS.put("darkseagreen4", 0x698B69);
        COLORS.put("grey95", 0xF2F2F2);
        COLORS.put("grey94", 0xF0F0F0);
        COLORS.put("darkseagreen2", 0xB4EEB4);
        COLORS.put("grey93", 0xEDEDED);
        COLORS.put("darkseagreen3", 0x9BCD9B);
        COLORS.put("grey92", 0xEBEBEB);
        COLORS.put("khaki1", 0xFFF68F);
        COLORS.put("grey78", 0xC7C7C7);
        COLORS.put("grey79", 0xC9C9C9);
        COLORS.put("darkslateblue", 0x483D8B);
        COLORS.put("antiquewhite", 0xFAEBD7);
        COLORS.put("bisque3", 0xCDB79E);
        COLORS.put("bisque4", 0x8B7D6B);
        COLORS.put("bisque1", 0xFFE4C4);
        COLORS.put("bisque2", 0xEED5B7);
        COLORS.put("grey80", 0xCCCCCC);
        COLORS.put("grey86", 0xDBDBDB);
        COLORS.put("grey85", 0xD9D9D9);
        COLORS.put("grey88", 0xE0E0E0);
        COLORS.put("grey87", 0xDEDEDE);
        COLORS.put("grey82", 0xD1D1D1);
        COLORS.put("khaki4", 0x8B864E);
        COLORS.put("grey81", 0xCFCFCF);
        COLORS.put("khaki3", 0xCDC673);
        COLORS.put("grey84", 0xD6D6D6);
        COLORS.put("grey83", 0xD4D4D4);
        COLORS.put("khaki2", 0xEEE685);
        COLORS.put("red3", 0xCD0000);
        COLORS.put("red4", 0x8B0000);
        COLORS.put("red1", 0xFF0000);
        COLORS.put("honeydew", 0xF0FFF0);
        COLORS.put("red2", 0xEE0000);
        COLORS.put("mediumpurple", 0x9370DB);
        COLORS.put("darkolivegreen2", 0xBCEE68);
        COLORS.put("brown1", 0xFF4040);
        COLORS.put("darkolivegreen3", 0xA2CD5A);
        COLORS.put("darkolivegreen4", 0x6E8B3D);
        COLORS.put("brown4", 0x8B2323);
        COLORS.put("brown2", 0xEE3B3B);
        COLORS.put("lightslategray", 0x778899);
        COLORS.put("brown3", 0xCD3333);
        COLORS.put("darkolivegreen1", 0xCAFF70);
        COLORS.put("slateblue3", 0x6959CD);
        COLORS.put("slateblue4", 0x473C8B);
        COLORS.put("lightcoral", 0xF08080);
        COLORS.put("seagreen4", 0x2E8B57);
        COLORS.put("slateblue2", 0x7A67EE);
        COLORS.put("seagreen1", 0x54FF9F);
        COLORS.put("slateblue1", 0x836FFF);
        COLORS.put("seagreen3", 0x43CD80);
        COLORS.put("seagreen2", 0x4EEE94);
        COLORS.put("cadetblue2", 0x8EE5EE);
        COLORS.put("darkorchid2", 0xB23AEE);
        COLORS.put("cadetblue1", 0x98F5FF);
        COLORS.put("darkorchid1", 0xBF3EFF);
        COLORS.put("darkorchid4", 0x68228B);
        COLORS.put("darkorchid3", 0x9A32CD);
        COLORS.put("cadetblue4", 0x53868B);
        COLORS.put("cadetblue3", 0x7AC5CD);
        COLORS.put("coral", 0xFF7F50);
        COLORS.put("darksalmon", 0xE9967A);
        COLORS.put("grey100", 0xFFFFFF);
        COLORS.put("palegoldenrod", 0xEEE8AA);
        COLORS.put("azure2", 0xE0EEEE);
        COLORS.put("azure1", 0xF0FFFF);
        COLORS.put("azure4", 0x838B8B);
        COLORS.put("honeydew3", 0xC1CDC1);
        COLORS.put("azure3", 0xC1CDCD);
        COLORS.put("honeydew4", 0x838B83);
        COLORS.put("honeydew1", 0xF0FFF0);
        COLORS.put("honeydew2", 0xE0EEE0);
        COLORS.put("cornsilk2", 0xEEE8CD);
        COLORS.put("cornsilk1", 0xFFF8DC);
        COLORS.put("darkturquoise", 0x00CED1);
        COLORS.put("cornsilk4", 0x8B8878);
        COLORS.put("cornsilk3", 0xCDC8B1);
        COLORS.put("steelblue4", 0x36648B);
        COLORS.put("steelblue3", 0x4F94CD);
        COLORS.put("sandybrown", 0xF4A460);
        COLORS.put("steelblue2", 0x5CACEE);
        COLORS.put("sienna1", 0xFF8247);
        COLORS.put("steelblue1", 0x63B8FF);
        COLORS.put("navy", 0x000080);
        COLORS.put("hotpink", 0xFF69B4);
        COLORS.put("green3", 0x00CD00);
        COLORS.put("green4", 0x008B00);
        COLORS.put("grey22", 0x383838);
        COLORS.put("grey21", 0x363636);
        COLORS.put("grey20", 0x333333);
        COLORS.put("grey18", 0x2E2E2E);
        COLORS.put("grey19", 0x303030);
        COLORS.put("grey16", 0x292929);
        COLORS.put("grey17", 0x2B2B2B);
        COLORS.put("grey14", 0x242424);
        COLORS.put("grey15", 0x262626);
        COLORS.put("grey12", 0x1F1F1F);
        COLORS.put("snow", 0xFFFAFA);
        COLORS.put("grey13", 0x212121);
        COLORS.put("green2", 0x00EE00);
        COLORS.put("green1", 0x00FF00);
        COLORS.put("plum4", 0x8B668B);
        COLORS.put("plum1", 0xFFBBFF);
        COLORS.put("thistle3", 0xCDB5CD);
        COLORS.put("plum2", 0xEEAEEE);
        COLORS.put("thistle4", 0x8B7B8B);
        COLORS.put("plum3", 0xCD96CD);
        COLORS.put("thistle1", 0xFFE1FF);
        COLORS.put("ghostwhite", 0xF8F8FF);
        COLORS.put("thistle2", 0xEED2EE);
        COLORS.put("grey31", 0x4F4F4F);
        COLORS.put("grey30", 0x4D4D4D);
        COLORS.put("snow4", 0x8B8989);
        COLORS.put("grey33", 0x545454);
        COLORS.put("snow3", 0xCDC9C9);
        COLORS.put("grey32", 0x525252);
        COLORS.put("darkslategrey", 0x2F4F4F);
        COLORS.put("grey27", 0x454545);
        COLORS.put("grey28", 0x474747);
        COLORS.put("grey29", 0x4A4A4A);
        COLORS.put("snow1", 0xFFFAFA);
        COLORS.put("grey23", 0x3B3B3B);
        COLORS.put("gray100", 0xFFFFFF);
        COLORS.put("snow2", 0xEEE9E9);
        COLORS.put("grey24", 0x3D3D3D);
        COLORS.put("grey25", 0x404040);
        COLORS.put("grey26", 0x424242);
        COLORS.put("turquoise4", 0x00868B);
        COLORS.put("turquoise3", 0x00C5CD);
        COLORS.put("turquoise2", 0x00E5EE);
        COLORS.put("turquoise1", 0x00F5FF);
        COLORS.put("seashell2", 0xEEE5DE);
        COLORS.put("seashell1", 0xFFF5EE);
        COLORS.put("seashell4", 0x8B8682);
        COLORS.put("seashell3", 0xCDC5BF);
        COLORS.put("sienna", 0xA0522D);
        COLORS.put("peru", 0xCD853F);
        COLORS.put("orchid2", 0xEE7AE9);
        COLORS.put("orchid1", 0xFF83FA);
        COLORS.put("lightsteelblue", 0xB0C4DE);
        COLORS.put("orchid3", 0xCD69C9);
        COLORS.put("orchid4", 0x8B4789);
        COLORS.put("gold", 0xFFD700);
        COLORS.put("darkgray", 0xA9A9A9);
        COLORS.put("grey11", 0x1C1C1C);
        COLORS.put("grey10", 0x1A1A1A);
        COLORS.put("goldenrod1", 0xFFC125);
        COLORS.put("chocolate4", 0x8B4513);
        COLORS.put("goldenrod4", 0x8B6914);
        COLORS.put("goldenrod3", 0xCD9B1D);
        COLORS.put("goldenrod2", 0xEEB422);
        COLORS.put("lightsalmon", 0xFFA07A);
        COLORS.put("chocolate1", 0xFF7F24);
        COLORS.put("chocolate3", 0xCD661D);
        COLORS.put("chocolate2", 0xEE7621);
        COLORS.put("grey65", 0xA6A6A6);
        COLORS.put("grey9", 0x171717);
        COLORS.put("grey66", 0xA8A8A8);
        COLORS.put("grey8", 0x141414);
        COLORS.put("grey63", 0xA1A1A1);
        COLORS.put("grey7", 0x121212);
        COLORS.put("grey64", 0xA3A3A3);
        COLORS.put("grey61", 0x9C9C9C);
        COLORS.put("grey62", 0x9E9E9E);
        COLORS.put("grey60", 0x999999);
        COLORS.put("grey2", 0x050505);
        COLORS.put("grey1", 0x030303);
        COLORS.put("grey0", 0x000000);
        COLORS.put("grey6", 0x0F0F0F);
        COLORS.put("grey5", 0x0D0D0D);
        COLORS.put("grey4", 0x0A0A0A);
        COLORS.put("grey3", 0x080808);
        COLORS.put("darkblue", 0x00008B);
        COLORS.put("firebrick1", 0xFF3030);
        COLORS.put("firebrick2", 0xEE2C2C);
        COLORS.put("firebrick3", 0xCD2626);
        COLORS.put("firebrick4", 0x8B1A1A);
        COLORS.put("lightslateblue", 0x8470FF);
        COLORS.put("grey59", 0x969696);
        COLORS.put("grey58", 0x949494);
        COLORS.put("grey57", 0x919191);
        COLORS.put("grey56", 0x8F8F8F);
        COLORS.put("grey74", 0xBDBDBD);
        COLORS.put("grey75", 0xBFBFBF);
        COLORS.put("lawngreen", 0x7CFC00);
        COLORS.put("grey76", 0xC2C2C2);
        COLORS.put("grey77", 0xC4C4C4);
        COLORS.put("grey70", 0xB3B3B3);
        COLORS.put("grey71", 0xB5B5B5);
        COLORS.put("grey72", 0xB8B8B8);
        COLORS.put("grey73", 0xBABABA);
        COLORS.put("mediumspringgreen", 0x00FA9A);
        COLORS.put("lightsalmon4", 0x8B5742);
        COLORS.put("mediumpurple3", 0x8968CD);
        COLORS.put("lightsalmon1", 0xFFA07A);
        COLORS.put("mediumpurple4", 0x5D478B);
        COLORS.put("gainsboro", 0xDCDCDC);
        COLORS.put("mediumpurple1", 0xAB82FF);
        COLORS.put("lightsalmon3", 0xCD8162);
        COLORS.put("mediumpurple2", 0x9F79EE);
        COLORS.put("lightsalmon2", 0xEE9572);
        COLORS.put("floralwhite", 0xFFFAF0);
        COLORS.put("bisque", 0xFFE4C4);
        COLORS.put("lightgoldenrod4", 0x8B814C);
        COLORS.put("grey68", 0xADADAD);
        COLORS.put("lightgoldenrod1", 0xFFEC8B);
        COLORS.put("grey67", 0xABABAB);
        COLORS.put("lightgoldenrod2", 0xEEDC82);
        COLORS.put("lightgoldenrod3", 0xCDBE70);
        COLORS.put("grey69", 0xB0B0B0);
        COLORS.put("grey40", 0x666666);
        COLORS.put("grey43", 0x6E6E6E);
        COLORS.put("grey44", 0x707070);
        COLORS.put("grey41", 0x696969);
        COLORS.put("grey42", 0x6B6B6B);
        COLORS.put("lightseagreen", 0x20B2AA);
        COLORS.put("lightskyblue", 0x87CEFA);
        COLORS.put("grey37", 0x5E5E5E);
        COLORS.put("grey36", 0x5C5C5C);
        COLORS.put("grey35", 0x595959);
        COLORS.put("ivory", 0xFFFFF0);
        COLORS.put("grey34", 0x575757);
        COLORS.put("grey39", 0x636363);
        COLORS.put("grey38", 0x616161);
        COLORS.put("grey50", 0x7F7F7F);
        COLORS.put("grey51", 0x828282);
        COLORS.put("grey52", 0x858585);
        COLORS.put("grey53", 0x878787);
        COLORS.put("dimgray", 0x696969);
        COLORS.put("grey54", 0x8A8A8A);
        COLORS.put("grey55", 0x8C8C8C);
        COLORS.put("darkorange", 0xFF8C00);
        COLORS.put("indianred", 0xCD5C5C);
        COLORS.put("grey46", 0x757575);
        COLORS.put("grey45", 0x737373);
        COLORS.put("lightsteelblue3", 0xA2B5CD);
        COLORS.put("grey48", 0x7A7A7A);
        COLORS.put("lightsteelblue4", 0x6E7B8B);
        COLORS.put("grey47", 0x787878);
        COLORS.put("lightsteelblue1", 0xCAE1FF);
        COLORS.put("lightsteelblue2", 0xBCD2EE);
        COLORS.put("grey49", 0x7D7D7D);
        COLORS.put("gray32", 0x525252);
        COLORS.put("gray33", 0x545454);
        COLORS.put("rosybrown3", 0xCD9B9B);
        COLORS.put("rosybrown4", 0x8B6969);
        COLORS.put("gray31", 0x4F4F4F);
        COLORS.put("gray36", 0x5C5C5C);
        COLORS.put("gray37", 0x5E5E5E);
        COLORS.put("gray34", 0x575757);
        COLORS.put("hotpink2", 0xEE6AA7);
        COLORS.put("lightgoldenrod", 0xEEDD82);
        COLORS.put("hotpink1", 0xFF6EB4);
        COLORS.put("gray38", 0x616161);
        COLORS.put("gray39", 0x636363);
        COLORS.put("blanchedalmond", 0xFFEBCD);
        COLORS.put("tomato4", 0x8B3626);
        COLORS.put("tomato3", 0xCD4F39);
        COLORS.put("tomato2", 0xEE5C42);
        COLORS.put("darkkhaki", 0xBDB76B);
        COLORS.put("yellow4", 0x8B8B00);
        COLORS.put("hotpink3", 0xCD6090);
        COLORS.put("hotpink4", 0x8B3A62);
        COLORS.put("tomato1", 0xFF6347);
        COLORS.put("lightgrey", 0xD3D3D3);
        COLORS.put("lavenderblush2", 0xEEE0E5);
        COLORS.put("gray41", 0x696969);
        COLORS.put("lavenderblush1", 0xFFF0F5);
        COLORS.put("gray42", 0x6B6B6B);
        COLORS.put("lavenderblush4", 0x8B8386);
        COLORS.put("gray43", 0x6E6E6E);
        COLORS.put("lavenderblush3", 0xCDC1C5);
        COLORS.put("gray44", 0x707070);
        COLORS.put("gray46", 0x757575);
        COLORS.put("gray47", 0x787878);
        COLORS.put("gray48", 0x7A7A7A);
        COLORS.put("gray49", 0x7D7D7D);
        COLORS.put("skyblue3", 0x6CA6CD);
        COLORS.put("lightyellow1", 0xFFFFE0);
        COLORS.put("skyblue4", 0x4A708B);
        COLORS.put("navajowhite1", 0xFFDEAD);
        COLORS.put("navajowhite2", 0xEECFA1);
        COLORS.put("navajowhite3", 0xCDB38B);
        COLORS.put("mediumturquoise", 0x48D1CC);
        COLORS.put("aliceblue", 0xF0F8FF);
        COLORS.put("navajowhite4", 0x8B795E);
        COLORS.put("skyblue1", 0x87CEFF);
        COLORS.put("lightyellow3", 0xCDCDB4);
        COLORS.put("skyblue2", 0x7EC0EE);
        COLORS.put("lightyellow2", 0xEEEED1);
        COLORS.put("rosybrown2", 0xEEB4B4);
        COLORS.put("burlywood", 0xDEB887);
        COLORS.put("gray51", 0x828282);
        COLORS.put("rosybrown1", 0xFFC1C1);
        COLORS.put("lightyellow4", 0x8B8B7A);
        COLORS.put("gray50", 0x7F7F7F);
        COLORS.put("gray58", 0x949494);
        COLORS.put("gray59", 0x969696);
        COLORS.put("gray56", 0x8F8F8F);
        COLORS.put("gray57", 0x919191);
        COLORS.put("gray54", 0x8A8A8A);
        COLORS.put("gray52", 0x858585);
        COLORS.put("cornsilk", 0xFFF8DC);
        COLORS.put("gray53", 0x878787);
        COLORS.put("rosybrown", 0xBC8F8F);
        COLORS.put("chartreuse", 0x7FFF00);
        COLORS.put("firebrick", 0xB22222);
        COLORS.put("gray62", 0x9E9E9E);
        COLORS.put("gray61", 0x9C9C9C);
        COLORS.put("yellow3", 0xCDCD00);
        COLORS.put("olivedrab3", 0x9ACD32);
        COLORS.put("gray67", 0xABABAB);
        COLORS.put("yellow2", 0xEEEE00);
        COLORS.put("olivedrab2", 0xB3EE3A);
        COLORS.put("gray68", 0xADADAD);
        COLORS.put("yellow1", 0xFFFF00);
        COLORS.put("gray69", 0xB0B0B0);
        COLORS.put("olivedrab4", 0x698B22);
        COLORS.put("gray63", 0xA1A1A1);
        COLORS.put("gray64", 0xA3A3A3);
        COLORS.put("indianred1", 0xFF6A6A);
        COLORS.put("gray66", 0xA8A8A8);
        COLORS.put("palevioletred4", 0x8B475D);
        COLORS.put("darkslategray2", 0x8DEEEE);
        COLORS.put("indianred3", 0xCD5555);
        COLORS.put("darkslategray3", 0x79CDCD);
        COLORS.put("indianred2", 0xEE6363);
        COLORS.put("darkslategray4", 0x528B8B);
        COLORS.put("indianred4", 0x8B3A3A);
        COLORS.put("palevioletred1", 0xFF82AB);
        COLORS.put("palevioletred2", 0xEE799F);
        COLORS.put("olivedrab1", 0xC0FF3E);
        COLORS.put("palevioletred3", 0xCD6889);
        COLORS.put("darkslategray1", 0x97FFFF);
        COLORS.put("dimgrey", 0x696969);
        COLORS.put("gray71", 0xB5B5B5);
        COLORS.put("gray73", 0xBABABA);
        COLORS.put("mediumaquamarine", 0x66CDAA);
        COLORS.put("gray72", 0xB8B8B8);
        COLORS.put("gray77", 0xC4C4C4);
        COLORS.put("gray76", 0xC2C2C2);
        COLORS.put("gray74", 0xBDBDBD);
        COLORS.put("deepskyblue", 0x00BFFF);
        COLORS.put("gray79", 0xC9C9C9);
        COLORS.put("gray78", 0xC7C7C7);
        COLORS.put("gray83", 0xD4D4D4);
        COLORS.put("gray84", 0xD6D6D6);
        COLORS.put("gray81", 0xCFCFCF);
        COLORS.put("gray82", 0xD1D1D1);
        COLORS.put("lightgray", 0xD3D3D3);
        COLORS.put("maroon2", 0xEE30A7);
        COLORS.put("maroon3", 0xCD2990);
        COLORS.put("maroon1", 0xFF34B3);
        COLORS.put("lightpink", 0xFFB6C1);
        COLORS.put("maroon4", 0x8B1C62);
        COLORS.put("slateblue", 0x6A5ACD);
        COLORS.put("lemonchiffon1", 0xFFFACD);
        COLORS.put("gray86", 0xDBDBDB);
        COLORS.put("orange4", 0x8B5A00);
        COLORS.put("orange3", 0xCD8500);
        COLORS.put("lemonchiffon3", 0xCDC9A5);
        COLORS.put("gray88", 0xE0E0E0);
        COLORS.put("orange2", 0xEE9A00);
        COLORS.put("lemonchiffon2", 0xEEE9BF);
        COLORS.put("gray87", 0xDEDEDE);
        COLORS.put("orange1", 0xFFA500);
        COLORS.put("lemonchiffon4", 0x8B8970);
        COLORS.put("gray89", 0xE3E3E3);
        COLORS.put("darkgreen", 0x006400);
        COLORS.put("darkslategray", 0x2F4F4F);
        COLORS.put("gray91", 0xE8E8E8);
        COLORS.put("lightpink3", 0xCD8C95);
        COLORS.put("crimson", 0xDC143C);
        COLORS.put("gray92", 0xEBEBEB);
        COLORS.put("lightpink2", 0xEEA2AD);
        COLORS.put("lemonchiffon", 0xFFFACD);
        COLORS.put("gray93", 0xEDEDED);
        COLORS.put("lightpink1", 0xFFAEB9);
        COLORS.put("gray94", 0xF0F0F0);
        COLORS.put("seashell", 0xFFF5EE);
        COLORS.put("pink2", 0xEEA9B8);
        COLORS.put("magenta1", 0xFF00FF);
        COLORS.put("pink3", 0xCD919E);
        COLORS.put("magenta2", 0xEE00EE);
        COLORS.put("pink4", 0x8B636C);
        COLORS.put("magenta3", 0xCD00CD);
        COLORS.put("magenta4", 0x8B008B);
        COLORS.put("lightpink4", 0x8B5F65);
        COLORS.put("pink1", 0xFFB5C5);
        COLORS.put("antiquewhite2", 0xEEDFCC);
        COLORS.put("antiquewhite3", 0xCDC0B0);
        COLORS.put("antiquewhite4", 0x8B8378);
        COLORS.put("antiquewhite1", 0xFFEFDB);
        COLORS.put("deeppink", 0xFF1493);
        COLORS.put("gray99", 0xFCFCFC);
        COLORS.put("gray98", 0xFAFAFA);
        COLORS.put("gray97", 0xF7F7F7);
        COLORS.put("gray96", 0xF5F5F5);
        COLORS.put("whitesmoke", 0xF5F5F5);
        COLORS.put("wheat", 0xF5DEB3);
        COLORS.put("olivedrab", 0x6B8E23);
        COLORS.put("mediumblue", 0x0000CD);
        COLORS.put("wheat4", 0x8B7E66);
        COLORS.put("darkgrey", 0xA9A9A9);
        COLORS.put("wheat3", 0xCDBA96);
        COLORS.put("wheat2", 0xEED8AE);
        COLORS.put("wheat1", 0xFFE7BA);
        COLORS.put("deepskyblue3", 0x009ACD);
        COLORS.put("steelblue", 0x4682B4);
        COLORS.put("deepskyblue4", 0x00688B);
        COLORS.put("deepskyblue1", 0x00BFFF);
        COLORS.put("deepskyblue2", 0x00B2EE);
    }
}
