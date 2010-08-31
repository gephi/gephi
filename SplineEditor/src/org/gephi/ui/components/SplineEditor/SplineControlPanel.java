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
package org.gephi.ui.components.SplineEditor;

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

        JButton saveTemplate = addButton(debugPanel, "Enregistrer");
        JButton removeTemplate = addButton(debugPanel, "Supprimer");
        saveTemplate.setEnabled(false);
        removeTemplate.setEnabled(false);

        addEmptySpace(debugPanel, 6);

        addSeparator(debugPanel, NbBundle.getMessage(SplineEditor.class, "splineEditor_controls"));

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
