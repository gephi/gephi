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
import java.util.LinkedList;
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

public class StatisticsReporterImpl implements Printable {

    JEditorPane mDisplay;
    JButton mPrint;
    JButton mSave;
    int pageIndex = 0;
    int currentPage = -1;
    double pageStartY = 0;
    double pageEndY = 0;
    /*

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
    {
    System.out.println(pageIndex);
    try
    {
    View rootView = mDisplay.getUI().getRootView(mDisplay);
    Rectangle allocation = new Rectangle((int)pageFormat.getImageableX(),
    (int) pageFormat.getImageableY(),
    (int)pageFormat.getImageableWidth(),
    (int)pageFormat.getImageableHeight());

    double scale = pageFormat.getImageableWidth()/ mDisplay.getMinimumSize().getWidth();
    ((Graphics2D)graphics).scale(scale, scale);
    graphics.setClip((int) (pageFormat.getImageableX()/scale),
    (int) (pageFormat.getImageableY()/scale),
    (int) (pageFormat.getImageableWidth()/scale),
    (int) (pageFormat.getImageableHeight()/scale));



    //LinkedList<View> views = new LinkedList<View>();
    for (int i = 0; i < rootView.getViewCount(); i++)
    {
    Shape allocation1 = rootView.getChildAllocation(i,allocation);
    // if (childAllocation != null)
    {

    View childView = rootView.getView(i);

    for (int j = 0; j < childView.getViewCount(); j++)
    {
    Shape childAllocation = childView.getChildAllocation(j,allocation);
    View leafView = childView.getView(j);
    childView.paint(graphics, childAllocation);
    System.out.println(pageIndex + "\t" + i + "\t" + "\t" +j +"\t" + childView.getViewCount());


    }

    }
    }
    if(pageIndex > 3)
    return Printable.NO_SUCH_PAGE;


    }catch(Exception e){e.printStackTrace();}

    return Printable.PAGE_EXISTS;
    }
     */

    /**
     *
     * @param graphics
     * @param pageFormat
     * @param pageIndex
     * @return
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        try {
            double scale = 1.0;
            View rootView = mDisplay.getUI().getRootView(mDisplay);
            Graphics2D graphics2D = (Graphics2D) graphics;

            if ((mDisplay.getMinimumSize().getWidth() > pageFormat.getImageableWidth())) {
                scale = pageFormat.getImageableWidth() / mDisplay.getMinimumSize().getWidth();
                graphics2D.scale(scale, scale);
            }
            graphics2D.setClip((int) (pageFormat.getImageableX() / scale),
                    (int) (pageFormat.getImageableY() / scale),
                    (int) (pageFormat.getImageableWidth() / scale),
                    (int) (pageFormat.getImageableHeight() / scale));

            if (pageIndex > currentPage) {
                currentPage = pageIndex;
                pageStartY += pageEndY;
                pageEndY = graphics2D.getClipBounds().getHeight();
            }

            graphics2D.translate(graphics2D.getClipBounds().getX(),
                    graphics2D.getClipBounds().getY());

            Rectangle allocation = new Rectangle(0,
                    (int) -pageStartY,
                    (int) (mDisplay.getMinimumSize().getWidth()),
                    (int) (mDisplay.getPreferredSize().getHeight()));

            boolean valid = false;
            for (int i = 0; i < rootView.getViewCount(); i++) {
                Rectangle childAllocation = (Rectangle) rootView.getChildAllocation(i, allocation);
                if (childAllocation != null) {
                    View childView = rootView.getView(i);

                    childView.paint(graphics2D, allocation);
                    valid = true;
                }
            }

            /*
            if (printView(graphics2D, allocation, rootView))
            {
            return Printable.PAGE_EXISTS;
            }
            else {
            pageStartY = 0;
            pageEndY = 0;
            currentPage = -1;
            return Printable.NO_SUCH_PAGE;
            }*/

            if (valid) {
                return Printable.PAGE_EXISTS;
            } else {
                return Printable.NO_SUCH_PAGE;
            }


        } catch (Exception e) {
        }

        return Printable.NO_SUCH_PAGE;
    }

    protected boolean printView(Graphics2D graphics2D, Shape allocation, View view) {
        boolean pageExists = false;
        Rectangle clipRectangle = graphics2D.getClipBounds();
        Shape childAllocation;
        View childView;

        if (view.getViewCount() > 0 && !view.getElement().getName().equalsIgnoreCase("td")) {
            for (int i = 0; i < view.getViewCount(); i++) {
                childAllocation = view.getChildAllocation(i, allocation);
                if (childAllocation != null) {
                    childView = view.getView(i);
                    if (printView(graphics2D, childAllocation, childView)) {
                        pageExists = true;
                    }
                }
            }
        } else {
            if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
                pageExists = true;
                if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) &&
                        (allocation.intersects(clipRectangle))) {
                    view.paint(graphics2D, allocation);
                } else {
                    if (allocation.getBounds().getY() >= clipRectangle.getY()) {
                        if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
                            view.paint(graphics2D, allocation);
                        } else {
                            //  IV
                            if (allocation.getBounds().getY() < pageEndY) {
                                pageEndY = allocation.getBounds().getY();
                            }
                        }
                    }
                }
            }
        }

        return pageExists;
    }

    public void printButtonPushed() {
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = pjob.defaultPage();
        pjob.setPrintable(this, pf);

        try {
            if (pjob.printDialog()) {
                pjob.print();
            }
        } catch (PrinterException e) {
        }

    }

    /**
     * 
     * @param statistics
     */
    public StatisticsReporterImpl(Statistics statistics) {
        mDisplay = new JEditorPane();
        mDisplay.setEditable(false);
        mDisplay.setContentType("text/html;");
        String report = statistics.getReport();
        mDisplay.setText(report);
        mPrint = new JButton("Print");
        mPrint.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                printButtonPushed();
            }
        });

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



        Dimension dimension = new Dimension(500, 400);
        frame.setPreferredSize(dimension);

        //Display the window.
        frame.setSize(dimension);
        frame.setLocationRelativeTo(null);
        frame.setDefaultLookAndFeelDecorated(true);
        frame.pack();
        frame.setVisible(true);

    }
}
