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
package org.gephi.ui.components.splineeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.Evaluator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.openide.util.NbBundle;

class SplineControlPanel extends JPanel {

    private SplineDisplay display;
    private int linesCount = 0;
    private Animator controller;
    private SplineEditor editor;

    SplineControlPanel(SplineEditor editor) {
        super(new BorderLayout());
        this.editor = editor;

        add(buildEquationDisplay(), BorderLayout.CENTER);
        add(buildDebugControls(), BorderLayout.EAST);
    }

    private Component buildDebugControls() {
        JPanel debugPanel = new JPanel(new GridBagLayout());

        debugPanel.add(Box.createHorizontalStrut(150),
                new GridBagConstraints(0, linesCount++,
                2, 1,
                1.0, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0, 0));

        //        button = addButton(debugPanel, "Create");
        //        button.addActionListener(new ActionListener() {
        //            public void actionPerformed(ActionEvent e) {
        //                JFileChooser chooser = new JFileChooser(".");
        //                int choice = chooser.showSaveDialog(SplineControlPanel.this);
        //                if (choice == JFileChooser.CANCEL_OPTION) {
        //                    return;
        //                }
        //                File file = chooser.getSelectedFile();
        //                try {
        //                    OutputStream out = new FileOutputStream(file);
        //                    display.saveAsTemplate(out);
        //                    out.close();
        //                } catch (FileNotFoundException e1) {
        //                } catch (IOException e1) {
        //                }
        //            }
        //        });

        addSeparator(debugPanel, NbBundle.getMessage(SplineEditor.class, "splineEditor_templates"));
        debugPanel.add(createTemplates(),
                new GridBagConstraints(0, linesCount++,
                2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0, 0));

        addEmptySpace(debugPanel, 6);

        JButton closeButton = addButton(debugPanel, NbBundle.getMessage(SplineEditor.class, "splineEditor_close"));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                editor.dispose();
            }
        });

        addEmptySpace(debugPanel, 6);

        debugPanel.add(Box.createVerticalGlue(),
                new GridBagConstraints(0, linesCount++,
                2, 1,
                1.0, 1.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.WEST);
        wrapper.add(debugPanel);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        return wrapper;
    }

    private Component createTemplates() {
        DefaultListModel model = new DefaultListModel();
        model.addElement(createTemplate(0.0, 0.0, 1.0, 1.0));
        model.addElement(createTemplate(0.0, 1.0, 0.0, 1.0));
        model.addElement(createTemplate(0.0, 1.0, 1.0, 1.0));
        model.addElement(createTemplate(0.0, 1.0, 1.0, 0.0));
        model.addElement(createTemplate(1.0, 0.0, 0.0, 1.0));
        model.addElement(createTemplate(1.0, 0.0, 1.0, 1.0));
        model.addElement(createTemplate(1.0, 0.0, 1.0, 0.0));

        JList list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new TemplateCellRenderer());
        list.addListSelectionListener(new TemplateSelectionHandler());

        JScrollPane pane = new JScrollPane(list);
        pane.getViewport().setPreferredSize(new Dimension(98, 97 * 3));
        return pane;
    }

    private JButton addButton(JPanel debugPanel, String label) {
        JButton button;
        debugPanel.add(button = new JButton(label),
                new GridBagConstraints(0, linesCount++,
                2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(3, 0, 0, 0),
                0, 0));
        return button;
    }

    private String formatPoint(Point2D p) {
        NumberFormat formatter = getNumberFormatter();
        return "" + formatter.format(p.getX()) + ", " + formatter.format(p.getY());
    }

    private Component buildEquationDisplay() {
        JPanel panel = new JPanel(new BorderLayout());

        display = new SplineDisplay();
        /*display.addPropertyChangeListener("control1", new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent evt) {
         labelControl1.setText(formatPoint(display.getControl1()));
         }
         });*/

        panel.add(display, BorderLayout.NORTH);



        return panel;
    }

    private JLabel addDebugLabel(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        panel.add(labelComponent,
                new GridBagConstraints(0, linesCount,
                1, 1,
                0.5, 0.0,
                GridBagConstraints.LINE_END,
                GridBagConstraints.NONE,
                new Insets(0, 6, 0, 0),
                0, 0));
        labelComponent = new JLabel(value);
        panel.add(labelComponent,
                new GridBagConstraints(1, linesCount++,
                1, 1,
                0.5, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.NONE,
                new Insets(0, 6, 0, 0),
                0, 0));
        return labelComponent;
    }

    private void addEmptySpace(JPanel panel, int size) {
        panel.add(Box.createVerticalStrut(size),
                new GridBagConstraints(0, linesCount++,
                2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.VERTICAL,
                new Insets(6, 0, 0, 0),
                0, 0));
    }

    private void addSeparator(JPanel panel, String label) {
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.add(new JLabel(label),
                new GridBagConstraints(0, 0,
                1, 1,
                0.0, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0),
                0, 0));
        innerPanel.add(new JSeparator(),
                new GridBagConstraints(1, 0,
                1, 1,
                0.9, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 6, 0, 6),
                0, 0));
        panel.add(innerPanel,
                new GridBagConstraints(0, linesCount++,
                2, 1,
                1.0, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL,
                new Insets(6, 6, 6, 0),
                0, 0));
    }
    private Evaluator point2dInterpolator = new Point2DNonLinearInterpolator();

    private class Point2DNonLinearInterpolator extends Evaluator<Point2D> {

        private Point2D value;

        @Override
        public Point2D evaluate(Point2D v0, Point2D v1,
                float fraction) {
            Point2D value = (Point2D) v0.clone();
            if (v0 != v1) {
                double x = value.getX();
                x += (v1.getX() - v0.getX()) * fraction;
                double y = value.getY();
                y += (v1.getY() - v0.getY()) * fraction;
                value.setLocation(x, y);
            } else {
                value.setLocation(v0.getX(), v0.getY());
            }
            return value;
        }
    }

    private class TemplateSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }

            JList list = (JList) e.getSource();
            Template template = (Template) list.getSelectedValue();
            if (template != null) {
                if (controller != null && controller.isRunning()) {
                    controller.stop();
                }

                controller = new Animator(300,
                        new PropertySetter(display, "control1",
                        point2dInterpolator, display.getControl1(),
                        template.getControl1()));
                controller.setResolution(10);
                controller.addTarget(new PropertySetter(display, "control2",
                        point2dInterpolator, display.getControl2(),
                        template.getControl2()));

                controller.start();
            }
        }
    }

    private static NumberFormat getNumberFormatter() {
        NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter;
    }

    private static Template createTemplate(double x1, double y1, double x2, double y2) {
        return new Template(new Point2D.Double(x1, y1),
                new Point2D.Double(x2, y2));
    }

    private static class TemplateCellRenderer extends DefaultListCellRenderer {

        private boolean isSelected;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Template template = (Template) value;
            this.setBackground(Color.WHITE);
            this.setIcon(new ImageIcon(template.getImage()));
            this.isSelected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (isSelected) {
                g.setColor(new Color(0.0f, 0.0f, 0.7f, 0.1f));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private static class Template {

        private Point2D control1;
        private Point2D control2;
        private Image image;

        public Template(Point2D control1, Point2D control2) {
            this.control1 = control1;
            this.control2 = control2;
        }

        public Point2D getControl1() {
            return control1;
        }

        public Point2D getControl2() {
            return control2;
        }

        public Image getImage() {
            if (image == null) {
                NumberFormat formatter = getNumberFormatter();

                String name = "";
                name += formatter.format(control1.getX()) + '-' + formatter.format(control1.getY());
                name += '-';
                name += formatter.format(control2.getX()) + '-' + formatter.format(control2.getY());

                try {
                    image = ImageIO.read(getClass().getResourceAsStream("images/templates/" + name + ".png"));
                } catch (IOException e) {
                }
            }

            return image;
        }
    }

    public SplineDisplay getDisplay() {
        return display;
    }
}
