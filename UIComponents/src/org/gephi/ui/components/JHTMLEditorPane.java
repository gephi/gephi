/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.ui.components;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;


//Copied from org.netbeans.lib.profiler.ui.components
//Autors Ian Formanek & Jiri Sedlacek
public class JHTMLEditorPane extends JEditorPane implements HyperlinkListener, MouseListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    /** Private Writer that extracts correctly formatted string from HTMLDocument */
    private class ExtendedHTMLWriter extends HTMLWriter {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ExtendedHTMLWriter(Writer w, HTMLDocument doc, int pos, int len) {
            super(w, doc, pos, len);
            setLineLength(Integer.MAX_VALUE);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        protected boolean isSupportedBreakFlowTag(AttributeSet attr) {
            Object o = attr.getAttribute(StyleConstants.NameAttribute);

            if (o instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag) o;

                if ((tag == HTML.Tag.HTML) || (tag == HTML.Tag.HEAD) || (tag == HTML.Tag.BODY) || (tag == HTML.Tag.HR)) {
                    return false;
                }

                return (tag).breaksFlow();
            }

            return false;
        }

        protected void emptyTag(Element elem) throws BadLocationException, IOException {
            if (isSupportedBreakFlowTag(elem.getAttributes())) {
                writeLineSeparator();
            }

            if (matchNameAttribute(elem.getAttributes(), HTML.Tag.CONTENT)) {
                text(elem);
            }
        }

        protected void endTag(Element elem) throws IOException {
            if (isSupportedBreakFlowTag(elem.getAttributes())) {
                writeLineSeparator();
            }
        }

        protected void startTag(Element elem) throws IOException, BadLocationException {
        }
    }

    // --- Private classes for copy/paste support --------------------------------
    //
    // NOTE: only vertical formatting is correctly copy/pasted,
    //       horizontal formatting (ul, li) is ignored.
    /** Private TransferHandler that copies correctly formatted string from HTMLDocument to system clipboard */
    private class HTMLTextAreaTransferHandler extends TransferHandler {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            try {
                int selStart = getSelectionStart();
                int selLength = getSelectionEnd() - selStart;

                StringWriter plainTextWriter = new StringWriter();

                try {
                    new ExtendedHTMLWriter(plainTextWriter, (HTMLDocument) getDocument(), selStart, selLength).write();
                } catch (Exception e) {
                }

                String plainText = NcrToUnicode.decode(plainTextWriter.toString());
                clip.setContents(new StringSelection(plainText), null);

                if (action == TransferHandler.MOVE) {
                    getDocument().remove(selStart, selLength);
                }
            } catch (BadLocationException ble) {
            }
        }
    }

    /** Class for decoding strings from NCR to Unicode */
    private static class NcrToUnicode {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static Map<String, String> entities;

        //~ Methods --------------------------------------------------------------------------------------------------------------
        public static String decode(String str) {
            StringBuffer ostr = new StringBuffer();
            int i1 = 0;
            int i2 = 0;

            while (i2 < str.length()) {
                i1 = str.indexOf("&", i2); //NOI18N

                if (i1 == -1) {
                    ostr.append(str.substring(i2, str.length()));

                    break;
                }

                ostr.append(str.substring(i2, i1));
                i2 = str.indexOf(";", i1); //NOI18N

                if (i2 == -1) {
                    ostr.append(str.substring(i1, str.length()));

                    break;
                }

                String tok = str.substring(i1 + 1, i2);

                if (tok.charAt(0) == '#') { //NOI18N

                    if (tok.equals("#160")) { //NOI18N
                        ostr.append(getEntities().get("nbsp")); //NOI18N // Fixes Issue 92818, "&nbsp;" is resolved as "&#160;" before decoding, so redirecting back to "&nbsp;"
                    } else {
                        tok = tok.substring(1);

                        try {
                            int radix = 10;

                            if (tok.trim().charAt(0) == 'x') { //NOI18N
                                radix = 16;
                                tok = tok.substring(1, tok.length());
                            }

                            ostr.append((char) Integer.parseInt(tok, radix));
                        } catch (NumberFormatException exp) {
                            ostr.append('?'); //NOI18N
                        }
                    }
                } else {
                    tok = getEntities().get(tok);

                    if (tok != null) {
                        ostr.append(tok);
                    } else {
                        ostr.append('?'); //NOI18N
                    }
                }

                i2++;
            }

            return ostr.toString();
        }

        private static synchronized Map<String, String> getEntities() {
            if (entities == null) {
                entities = new Hashtable();
                //Quotation mark
                entities.put("quot", "\""); //NOI18N
                //Ampersand

                entities.put("amp", "\u0026"); //NOI18N
                //Less than

                entities.put("lt", "\u003C"); //NOI18N
                //Greater than

                entities.put("gt", "\u003E"); //NOI18N
                //Nonbreaking space

                entities.put("nbsp", "\u0020"); //NOI18N // Fixes Issue 92818, "\u00A0" (&nbsp; equivalent) is resolved as incorrect character, thus mapping to standard space
                //Inverted exclamation point

                entities.put("iexcl", "\u00A1"); //NOI18N
                //Cent sign

                entities.put("cent", "\u00A2"); //NOI18N
                //Pound sign

                entities.put("pound", "\u00A3"); //NOI18N
                //General currency sign

                entities.put("curren", "\u00A4"); //NOI18N
                //Yen sign

                entities.put("yen", "\u00A5"); //NOI18N
                //Broken vertical bar

                entities.put("brvbar", "\u00A6"); //NOI18N
                //Section sign

                entities.put("sect", "\u00A7"); //NOI18N
                //Umlaut

                entities.put("uml", "\u00A8"); //NOI18N
                //Copyright

                entities.put("copy", "\u00A9"); //NOI18N
                //Feminine ordinal

                entities.put("ordf", "\u00AA"); //NOI18N
                //Left angle quote

                entities.put("laquo", "\u00AB"); //NOI18N
                //Not sign

                entities.put("not", "\u00AC"); //NOI18N
                //Soft hyphen

                entities.put("shy", "\u00AD"); //NOI18N
                //Registered trademark

                entities.put("reg", "\u00AE"); //NOI18N
                //Macron accent

                entities.put("macr", "\u00AF"); //NOI18N
                //Degree sign

                entities.put("deg", "\u00B0"); //NOI18N
                //Plus or minus

                entities.put("plusmn", "\u00B1"); //NOI18N
                //Superscript 2

                entities.put("sup2", "\u00B2"); //NOI18N
                //Superscript 3

                entities.put("sup3", "\u00B3"); //NOI18N
                //Acute accent

                entities.put("acute", "\u00B4"); //NOI18N
                //Micro sign (Greek mu)

                entities.put("micro", "\u00B5"); //NOI18N
                //Paragraph sign

                entities.put("para", "\u00B6"); //NOI18N
                //Middle dot

                entities.put("middot", "\u00B7"); //NOI18N
                //Cedilla

                entities.put("cedil", "\u00B8"); //NOI18N
                //Superscript 1

                entities.put("sup1", "\u00B9"); //NOI18N
                //Masculine ordinal

                entities.put("ordm", "\u00BA"); //NOI18N
                //Right angle quote

                entities.put("raquo", "\u00BB"); //NOI18N
                //Fraction one-fourth

                entities.put("frac14", "\u00BC"); //NOI18N
                //Fraction one-half

                entities.put("frac12", "\u00BD"); //NOI18N
                //Fraction three-fourths

                entities.put("frac34", "\u00BE"); //NOI18N
                //Inverted question mark

                entities.put("iquest", "\u00BF"); //NOI18N
                //Capital A, grave accent

                entities.put("Agrave", "\u00C0"); //NOI18N
                //Capital A, acute accent

                entities.put("Aacute", "\u00C1"); //NOI18N
                //Capital A, circumflex accent

                entities.put("Acirc", "\u00C2"); //NOI18N
                //Capital A, tilde

                entities.put("Atilde", "\u00C3"); //NOI18N
                //Capital A, umlaut

                entities.put("Auml", "\u00C4"); //NOI18N
                //Capital A, ring

                entities.put("Aring", "\u00C5"); //NOI18N
                //Capital AE ligature

                entities.put("AElig", "\u00C6"); //NOI18N
                //Capital C, cedilla

                entities.put("Ccedil", "\u00C7"); //NOI18N
                //Capital E, grave accent

                entities.put("Egrave", "\u00C8"); //NOI18N
                //Capital E, acute accent

                entities.put("Eacute", "\u00C9"); //NOI18N
                //Capital E, circumflex accent

                entities.put("Ecirc", "\u00CA"); //NOI18N
                //Capital E, umlaut

                entities.put("Euml", "\u00CB"); //NOI18N
                //Capital I, grave accent

                entities.put("Igrave", "\u00CC"); //NOI18N
                //Capital I, acute accent

                entities.put("Iacute", "\u00CD"); //NOI18N
                //Capital I, circumflex accent

                entities.put("Icirc", "\u00CE"); //NOI18N
                //Capital I, umlaut

                entities.put("Iuml", "\u00CF"); //NOI18N
                //Capital eth, Icelandic

                entities.put("ETH", "\u00D0"); //NOI18N
                //Capital N, tilde

                entities.put("Ntilde", "\u00D1"); //NOI18N
                //Capital O, grave accent

                entities.put("Ograve", "\u00D2"); //NOI18N
                //Capital O, acute accent

                entities.put("Oacute", "\u00D3"); //NOI18N
                //Capital O, circumflex accent

                entities.put("Ocirc", "\u00D4"); //NOI18N
                //Capital O, tilde

                entities.put("Otilde", "\u00D5"); //NOI18N
                //Capital O, umlaut

                entities.put("Ouml", "\u00D6"); //NOI18N
                //Multiply sign

                entities.put("times", "\u00D7"); //NOI18N
                //Capital O, slash

                entities.put("Oslash", "\u00D8"); //NOI18N
                //Capital U, grave accent

                entities.put("Ugrave", "\u00D9"); //NOI18N
                //Capital U, acute accent

                entities.put("Uacute", "\u00DA"); //NOI18N
                //Capital U, circumflex accent

                entities.put("Ucirc", "\u00DB"); //NOI18N
                //Capital U, umlaut

                entities.put("Uuml", "\u00DC"); //NOI18N
                //Capital Y, acute accent

                entities.put("Yacute", "\u00DD"); //NOI18N
                //Capital thorn, Icelandic

                entities.put("THORN", "\u00DE"); //NOI18N
                //Small sz ligature, German

                entities.put("szlig", "\u00DF"); //NOI18N
                //Small a, grave accent

                entities.put("agrave", "\u00E0"); //NOI18N
                //Small a, acute accent

                entities.put("aacute", "\u00E1"); //NOI18N
                //Small a, circumflex accent

                entities.put("acirc", "\u00E2"); //NOI18N
                //Small a, tilde

                entities.put("atilde", "\u00E3"); //NOI18N
                //Small a, umlaut

                entities.put("auml", "\u00E4"); //NOI18N
                //Small a, ring

                entities.put("aring", "\u00E5"); //NOI18N
                //Small ae ligature

                entities.put("aelig", "\u00E6"); //NOI18N
                //Small c, cedilla

                entities.put("ccedil", "\u00E7"); //NOI18N
                //Small e, grave accent

                entities.put("egrave", "\u00E8"); //NOI18N
                //Small e, acute accent

                entities.put("eacute", "\u00E9"); //NOI18N
                //Small e, circumflex accent

                entities.put("ecirc", "\u00EA"); //NOI18N
                //Small e, umlaut

                entities.put("euml", "\u00EB"); //NOI18N
                //Small i, grave accent

                entities.put("igrave", "\u00EC"); //NOI18N
                //Small i, acute accent

                entities.put("iacute", "\u00ED"); //NOI18N
                //Small i, circumflex accent

                entities.put("icirc", "\u00EE"); //NOI18N
                //Small i, umlaut

                entities.put("iuml", "\u00EF"); //NOI18N
                //Small eth, Icelandic

                entities.put("eth", "\u00F0"); //NOI18N
                //Small n, tilde

                entities.put("ntilde", "\u00F1"); //NOI18N
                //Small o, grave accent

                entities.put("ograve", "\u00F2"); //NOI18N
                //Small o, acute accent

                entities.put("oacute", "\u00F3"); //NOI18N
                //Small o, circumflex accent

                entities.put("ocirc", "\u00F4"); //NOI18N
                //Small o, tilde

                entities.put("otilde", "\u00F5"); //NOI18N
                //Small o, umlaut

                entities.put("ouml", "\u00F6"); //NOI18N
                //Division sign

                entities.put("divide", "\u00F7"); //NOI18N
                //Small o, slash

                entities.put("oslash", "\u00F8"); //NOI18N
                //Small u, grave accent

                entities.put("ugrave", "\u00F9"); //NOI18N
                //Small u, acute accent

                entities.put("uacute", "\u00FA"); //NOI18N
                //Small u, circumflex accent

                entities.put("ucirc", "\u00FB"); //NOI18N
                //Small u, umlaut

                entities.put("uuml", "\u00FC"); //NOI18N
                //Small y, acute accent

                entities.put("yacute", "\u00FD"); //NOI18N
                //Small thorn, Icelandic

                entities.put("thorn", "\u00FE"); //NOI18N
                //Small y, umlaut

                entities.put("yuml", "\u00FF"); //NOI18N
            }

            return entities;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.gephi.ui.components.Bundle"); // NOI18N
    private static final String CUT_STRING = messages.getString("HTMLTextArea_CutString"); // NOI18N
    private static final String COPY_STRING = messages.getString("HTMLTextArea_CopyString"); // NOI18N
    private static final String PASTE_STRING = messages.getString("HTMLTextArea_PasteString"); // NOI18N
    private static final String DELETE_STRING = messages.getString("HTMLTextArea_DeleteString"); // NOI18N
    private static final String SELECT_ALL_STRING = messages.getString("HTMLTextArea_SelectAllString"); // NOI18N
    // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private ActionListener popupListener;
    private JMenuItem itemCopy;
    private JMenuItem itemCut;
    private JMenuItem itemDelete;
    private JMenuItem itemPaste;
    private JMenuItem itemSelectAll;

    // --- Popup menu support ----------------------------------------------------
    private JPopupMenu popupMenu;
    private String originalText;
    private boolean showPopup = true;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public JHTMLEditorPane() {
        super();
        setEditorKit(new HTMLEditorKit());
        setEditable(false);
        setOpaque(true);
        setAutoscrolls(true);
        addHyperlinkListener(this);
        setTransferHandler(new HTMLTextAreaTransferHandler());
        setFont(UIManager.getFont("Label.font")); //NOI18N
        addMouseListener(this);
    }

    public JHTMLEditorPane(String text) {
        this();
        setText(text);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public void setForeground(Color color) {
        super.setForeground(color);
        setText(originalText);
    }

    public void setShowPopup(boolean showPopup) {
        this.showPopup = showPopup;
    }

    public boolean getShowPopup() {
        return showPopup;
    }

    public void setText(String value) {
        if (value == null) {
            return;
        }

        originalText = value;

        Font font = getFont();
        Color textColor = getForeground();
        value = value.replaceAll("\\n\\r|\\r\\n|\\n|\\r", "<br>"); //NOI18N
        value = value.replaceAll("<code>", "<code style=\"font-size: " + font.getSize() + "pt;\">"); //NOI18N

        String colorText = "rgb(" + textColor.getRed() + "," + textColor.getGreen() + "," + textColor.getBlue() + ")"; //NOI18N
        super.setText("<html><body text=\"" + colorText + "\" style=\"font-size: " + font.getSize() + "pt; font-family: " + font.getName() + ";\">" + value + "</body></html>"); //NOI18N
    }

    public void deleteSelection() {
        try {
            getDocument().remove(getSelectionStart(), getSelectionEnd() - getSelectionStart());
        } catch (Exception ex) {
        }

        ;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (!isEnabled()) {
            return;
        }

        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            showURL(e.getURL());
        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (isEnabled() && isFocusable() && showPopup) {
                JPopupMenu popup = getPopupMenu();

                if (popup != null) {
                    updatePopupMenu();

                    if (!hasFocus()) {
                        requestFocus(); // required for Select All functionality
                    }

                    popup.show(this, e.getX(), e.getY());
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void paste() {
        try {
            replaceSelection(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor).toString());
        } catch (Exception ex) {
        }
    }

    protected JPopupMenu getPopupMenu() {
        if (popupMenu == null) {
            popupMenu = createPopupMenu();
        }

        return popupMenu;
    }

    protected JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        popupListener = createPopupListener();

        itemCut = new JMenuItem(CUT_STRING);
        itemCopy = new JMenuItem(COPY_STRING);
        itemPaste = new JMenuItem(PASTE_STRING);
        itemDelete = new JMenuItem(DELETE_STRING);
        itemSelectAll = new JMenuItem(SELECT_ALL_STRING);

        itemCut.addActionListener(popupListener);
        itemCopy.addActionListener(popupListener);
        itemPaste.addActionListener(popupListener);
        itemDelete.addActionListener(popupListener);
        itemSelectAll.addActionListener(popupListener);

        popup.add(itemCut);
        popup.add(itemCopy);
        popup.add(itemPaste);
        popup.add(itemDelete);
        popup.addSeparator();
        popup.add(itemSelectAll);

        return popup;
    }

    protected void showURL(URL url) {
        // override to react to URL clicks
    }

    protected void updatePopupMenu() {
        // Cut
        itemCut.setEnabled(isEditable() && (getSelectedText() != null));

        // Copy
        itemCopy.setEnabled(getSelectedText() != null);

        // Paste
        try {
            Transferable clipboardContent = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
            itemPaste.setEnabled(isEditable() && (clipboardContent != null) && clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor));
        } catch (Exception e) {
            itemPaste.setEnabled(false);
        }

        // Delete
        if (isEditable()) {
            itemDelete.setVisible(true);
            itemDelete.setEnabled(getSelectedText() != null);
        } else {
            itemDelete.setVisible(false);
        }

    // Select All
    // always visible and enabled...
    }

    private ActionListener createPopupListener() {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == itemCut) {
                    cut();
                } else if (e.getSource() == itemCopy) {
                    copy();
                } else if (e.getSource() == itemPaste) {
                    paste();
                } else if (e.getSource() == itemDelete) {
                    deleteSelection();
                } else if (e.getSource() == itemSelectAll) {
                    selectAll();
                }
            }
        };
    }
}
