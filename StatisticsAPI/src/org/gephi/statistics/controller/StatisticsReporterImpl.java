/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gephi.statistics.controller;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.View;
import org.gephi.statistics.api.Statistics;



public class StatisticsReporterImpl implements Printable
{

    JEditorPane mDisplay;
    JButton mPrint;
    JButton mSave;
    int pageIndex = 0;
    int currentPage = -1;
    double pageStartY  = 0;
    double pageEndY = 0;


     public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
     {
         return -1;
     }






    public void printButtonPushed()
    {
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = pjob.defaultPage();
        pjob.setPrintable(this, pf);

        try
        {
            if (pjob.printDialog())
            {
                pjob.print();
            }
        } catch (PrinterException e) {}
    
    }


    /**
     * 
     * @param statistics
     */
    public  StatisticsReporterImpl(Statistics statistics)
    {
        mDisplay = new JEditorPane();
        mDisplay.setEditable(false);
        mDisplay.setContentType("text/html;");
        String report = statistics.getReport();
        mDisplay.setText(report);
        mPrint = new JButton("Print");
        mPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printButtonPushed();
        }});

        //Put the editor pane in a scroll pane.
        JScrollPane editorScrollPane = new JScrollPane(mDisplay);
        editorScrollPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(500, 400));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));





        JFrame frame = new JFrame("Results");
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(mPrint);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
        frame.getContentPane().add(editorScrollPane);



        Dimension dimension = new Dimension(500,400);
        frame.setPreferredSize(dimension);

        //Display the window.
        frame.setSize(dimension);
        frame.setLocationRelativeTo(null);
        frame.setDefaultLookAndFeelDecorated(true);
        frame.pack();
        frame.setVisible(true);
       
    }

}
