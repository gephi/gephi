/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, 
          Patick J. McSweeney <pjmcswee@syr.edu>
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
package org.gephi.desktop.statistics;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.WindowConstants;
import javax.swing.text.View;
import org.apache.commons.codec.binary.Base64;
import org.gephi.ui.components.JHTMLEditorPane;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 * @author Patick J. McSweeney
 */
class ReportSelection implements Transferable {

    private static ArrayList flavors = new ArrayList();

    static {
        try {
            flavors.add(new DataFlavor("text/html;class=java.lang.String"));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    private String html;

    /**
     *
     * @param html
     */
    public ReportSelection(String html) {
        this.html = html;
        String newHTML = new String();
        String[] result = html.split("file:");
        boolean first = true;
        for (int i = 0; i < result.length; i++) {
            if (result[i].contains("</IMG>")) {
                String next = result[i];
                //System.out.println(">  " + next);
                String[] elements = next.split("\"");
                String filename = elements[0];


                ByteArrayOutputStream out = new ByteArrayOutputStream();

                File file = new File(filename);
                try {
                    BufferedImage image = ImageIO.read(file);
                    ImageIO.write((RenderedImage) image, "PNG", out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] imageBytes = out.toByteArray();
                String base64String = Base64.encodeBase64String(imageBytes);
                if (!first) {

                    newHTML += "\"";
                }
                first = false;
                newHTML += "data:image/png;base64," + base64String;
                for (int j = 1; j < elements.length; j++) {
                    newHTML += "\"" + elements[j];
                }
            } else {
                newHTML += result[i];
            }
        }
        this.html = newHTML;
    }

    /**
     *
     * @return
     */
    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
    }

    /**
     *
     * @param flavor
     * @return
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    /**
     *
     * @param flavor
     * @return
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (String.class.equals(flavor.getRepresentationClass())) {
            return html;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}

/**
 *
 * @author pjmcswee
 */
public class StatisticsReportPanel extends javax.swing.JDialog implements Printable {

    private String mHTMLReport;

    public StatisticsReportPanel(java.awt.Frame parent, String html) {
        super(parent, false);
        mHTMLReport = html;
        initComponents();
        displayPane.setContentType("text/html;");
        displayPane.setText(this.mHTMLReport);
        Dimension dimension = new Dimension(700, 600);
        setPreferredSize(dimension);
        displayPane.setCaretPosition(0);
        setTitle("HTML Report");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        displayPane = (javax.swing.JEditorPane)(new JHTMLEditorPane());
        closeButton = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        printButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(displayPane);

        closeButton.setText(org.openide.util.NbBundle.getMessage(StatisticsReportPanel.class, "StatisticsReportPanel.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/statistics/resources/print.png"))); // NOI18N
        printButton.setText(org.openide.util.NbBundle.getMessage(StatisticsReportPanel.class, "StatisticsReportPanel.printButton.text")); // NOI18N
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(printButton);

        copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/statistics/resources/copy.gif"))); // NOI18N
        copyButton.setText(org.openide.util.NbBundle.getMessage(StatisticsReportPanel.class, "StatisticsReportPanel.copyButton.text")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(copyButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/statistics/resources/save.png"))); // NOI18N
        saveButton.setText(org.openide.util.NbBundle.getMessage(StatisticsReportPanel.class, "StatisticsReportPanel.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(saveButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeButton)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = pjob.defaultPage();
        pjob.setPrintable(this, pf);

        try {
            if (pjob.printDialog()) {
                pjob.print();
            }
        } catch (PrinterException e) {
            e.printStackTrace();
        }
}//GEN-LAST:event_printButtonActionPerformed
    private final String LAST_PATH = "StatisticsReportPanel_Save_Last_Path";

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        final String html = this.mHTMLReport;

        final String path = NbPreferences.forModule(StatisticsReportPanel.class).get(LAST_PATH, null);
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File destinationFolder = fileChooser.getSelectedFile();
            NbPreferences.forModule(StatisticsReportPanel.class).put(LAST_PATH, destinationFolder.getAbsolutePath());
            Thread saveReportThread = new Thread(new Runnable() {

                public void run() {
                    try {
                        saveReport(html, destinationFolder);
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StatisticsReportPanel.class, "StatisticsReportPanel.status.saveSuccess", destinationFolder.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "SaveReportTask");
            saveReportThread.start();

        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void saveReport(String html, File destinationFolder) throws IOException {

        if (!destinationFolder.exists()) {
            destinationFolder.mkdir();
        }

        //Find images location
        String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern pattern = Pattern.compile(imgRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        StringBuffer replaceBuffer = new StringBuffer();
        while (matcher.find()) {
            String fileAbsolutePath = matcher.group(1);
            if (fileAbsolutePath.startsWith("file:")) {
                fileAbsolutePath = fileAbsolutePath.replaceFirst("file:", "");
            }
            File file = new File(fileAbsolutePath);
            if (file.exists()) {
                copy(file, destinationFolder);
            }

            //Replace temp path
            matcher.appendReplacement(replaceBuffer, "<IMG SRC=\"" + file.getName() + "\">");
        }
        matcher.appendTail(replaceBuffer);

        //Write HTML file
        File htmlFile = new File(destinationFolder, "report.html");
        FileOutputStream outputStream = new FileOutputStream(htmlFile);
        OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");
        out.append(replaceBuffer.toString());
        out.flush();
        out.close();
        outputStream.close();
    }

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            toolkit.getSystemClipboard().setContents(new ReportSelection(this.mHTMLReport), null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_copyButtonActionPerformed

    /**
     * 
     * @param evt
     */
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose(); // TODO add your handling code here:
    }//GEN-LAST:event_closeButtonActionPerformed
    /**
     * @param args the command line arguments
    
    public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
    public void run() {
    StatisticsReportPanel dialog = new StatisticsReportPanel(new javax.swing.JFrame(), true);
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
    public void windowClosing(java.awt.event.WindowEvent e) {
    System.exit(0);
    }
    });
    dialog.setVisible(true);
    }
    });
    }
     * */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton copyButton;
    private javax.swing.JEditorPane displayPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton printButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

    /**
     *
     * @param graphics
     * @param pageFormat
     * @param pageIndex
     * @return
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {

        boolean last = false;
        try {

            View rootView = displayPane.getUI().getRootView(displayPane);

            double scaleX = pageFormat.getImageableWidth() / displayPane.getMinimumSize().getWidth();

            scaleX = Math.min(scaleX, 1.0);
            double scaleY = scaleX;

            int end = (int) (pageIndex * ((1.0f / scaleY) * (double) pageFormat.getImageableHeight()));
            Rectangle allocation = new Rectangle(0,
                    -end,
                    (int) pageFormat.getImageableWidth(),
                    (int) pageFormat.getImageableHeight());
            ((Graphics2D) graphics).scale(scaleX, scaleY);

            graphics.setClip((int) (pageFormat.getImageableX() / scaleX),
                    (int) (pageFormat.getImageableY() / scaleY),
                    (int) (pageFormat.getImageableWidth() / scaleX),
                    (int) (pageFormat.getImageableHeight() / scaleY));

            ((Graphics2D) graphics).translate(((Graphics2D) graphics).getClipBounds().getX(),
                    ((Graphics2D) graphics).getClipBounds().getY());

            rootView.paint(graphics, allocation);

            last = end > displayPane.getUI().getPreferredSize(displayPane).getHeight();

            if ((last)) {
                return Printable.NO_SUCH_PAGE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Printable.PAGE_EXISTS;
    }

    public void copy(File source, File dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            if (dest.isDirectory()) {
                dest = new File(dest, source.getName());
            }
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

            out.write(buf);

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
