/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * UIExporterPDFPanel.java
 *
 * Created on 13 avr. 2010, 18:21:09
 */
package org.gephi.ui.exporter.preview;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.lib.validation.PositiveNumberValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class UIExporterPDFPanel extends javax.swing.JPanel {

    private static final double INCH = 72.0;
    private static final double MM = 2.8346456692895527;
    private final String customSizeString;
    private boolean millimeter = true;
    private NumberFormat sizeFormatter;
    private NumberFormat marginFormatter;

    public UIExporterPDFPanel() {
        initComponents();

        sizeFormatter = NumberFormat.getNumberInstance();
        sizeFormatter.setMaximumFractionDigits(3);
        marginFormatter = NumberFormat.getNumberInstance();
        marginFormatter.setMaximumFractionDigits(1);

        //Page size model - http://en.wikipedia.org/wiki/Paper_size
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(new PageSizeItem(PageSize.A0, "A0", 841, 1189, 33.1, 46.8));
        comboBoxModel.addElement(new PageSizeItem(PageSize.A1, "A1", 594, 841, 23.4, 33.1));
        comboBoxModel.addElement(new PageSizeItem(PageSize.A2, "A2", 420, 594, 16.5, 23.4));
        comboBoxModel.addElement(new PageSizeItem(PageSize.A3, "A3", 297, 420, 11.7, 16.5));
        comboBoxModel.addElement(new PageSizeItem(PageSize.A4, "A4", 210, 297, 8.3, 11.7));
        comboBoxModel.addElement(new PageSizeItem(PageSize.A5, "A5", 148, 210, 5.8, 8.3));
        comboBoxModel.addElement(new PageSizeItem(PageSize.ARCH_A, "ARCH A", 229, 305, 9, 12));
        comboBoxModel.addElement(new PageSizeItem(PageSize.ARCH_B, "ARCH B", 305, 457, 12, 18));
        comboBoxModel.addElement(new PageSizeItem(PageSize.ARCH_C, "ARCH C", 457, 610, 18, 24));
        comboBoxModel.addElement(new PageSizeItem(PageSize.ARCH_D, "ARCH D", 610, 914, 24, 36));
        comboBoxModel.addElement(new PageSizeItem(PageSize.ARCH_E, "ARCH E", 914, 1219, 36, 48));
        comboBoxModel.addElement(new PageSizeItem(PageSize.B0, "B0", 1000, 1414, 39.4, 55.7));
        comboBoxModel.addElement(new PageSizeItem(PageSize.B1, "B1", 707, 1000, 27.8, 39.4));
        comboBoxModel.addElement(new PageSizeItem(PageSize.B2, "B2", 500, 707, 19.7, 27.8));
        comboBoxModel.addElement(new PageSizeItem(PageSize.B3, "B3", 353, 500, 13.9, 19.7));
        comboBoxModel.addElement(new PageSizeItem(PageSize.B4, "B4", 250, 353, 9.8, 13.9));
        comboBoxModel.addElement(new PageSizeItem(PageSize.B5, "B5", 176, 250, 6.9, 9.8));
        comboBoxModel.addElement(new PageSizeItem(PageSize.LEDGER, "Ledger", 432, 279, 17, 11));
        comboBoxModel.addElement(new PageSizeItem(PageSize.LEGAL, "Legal", 216, 356, 8.5, 14));
        comboBoxModel.addElement(new PageSizeItem(PageSize.LETTER, "Letter", 216, 279, 8.5, 11));
        comboBoxModel.addElement(new PageSizeItem(PageSize.TABLOID, "Tabloid", 279, 432, 11, 17));

        customSizeString = NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.pageSize.custom");
        comboBoxModel.addElement(customSizeString);
        pageSizeCombo.setModel(comboBoxModel);

        loadPreferences();

        initEvents();
        refreshUnit(false);
    }

    private void loadPreferences() {
        boolean defaultMM = NbPreferences.forModule(UIExporterPDF.class).getBoolean("Default_Millimeter", false);
        millimeter = NbPreferences.forModule(UIExporterPDF.class).getBoolean("Millimeter", defaultMM);
    }

    private void savePreferences() {
        NbPreferences.forModule(UIExporterPDF.class).putBoolean("Millimeter", millimeter);
    }

    private void initEvents() {
        pageSizeCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                Object selectedItem = pageSizeCombo.getSelectedItem();
                if (selectedItem != customSizeString) {
                    PageSizeItem pageSize = (PageSizeItem) selectedItem;
                    setPageSize(pageSize);
                }
            }
        });

        widthTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updatePageSize();
            }
        });

        heightTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updatePageSize();
            }
        });
        unitLink.setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                millimeter = !millimeter;
                refreshUnit(true);
            }
        });
    }

    public static ValidationPanel createValidationPanel(UIExporterPDFPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        //Size
        group.add(innerPanel.widthTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                new PositiveSizeValidator(innerPanel));
        group.add(innerPanel.heightTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                new PositiveSizeValidator(innerPanel));

        //Margins
        group.add(innerPanel.topMarginTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                Validators.REQUIRE_VALID_NUMBER);
        group.add(innerPanel.bottomMarginTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                Validators.REQUIRE_VALID_NUMBER);
        group.add(innerPanel.leftMarginTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                Validators.REQUIRE_VALID_NUMBER);
        group.add(innerPanel.rightMargintextField, Validators.REQUIRE_NON_EMPTY_STRING,
                Validators.REQUIRE_VALID_NUMBER);

        return validationPanel;
    }

    public void setup(PDFExporter pdfExporter) {
        DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) pageSizeCombo.getModel();
        PageSizeItem pageSize = new PageSizeItem(pdfExporter.getPageSize());
        int index = 0;
        if ((index = comboBoxModel.getIndexOf(pageSize)) == -1) {
            comboBoxModel.setSelectedItem(customSizeString);
        } else {
            pageSize = (PageSizeItem) comboBoxModel.getElementAt(index);
            comboBoxModel.setSelectedItem(pageSize);
        }

        setPageSize(pageSize);
        setMargins(pdfExporter.getMarginTop(), pdfExporter.getMarginBottom(), pdfExporter.getMarginLeft(), pdfExporter.getMarginRight());
        setOrientation(pdfExporter.isLandscape());
    }

    public void unsetup(PDFExporter pdfExporter) {
        if (pageSizeCombo.getSelectedItem() == customSizeString) {
            double width = pdfExporter.getPageSize().getWidth();
            double height = pdfExporter.getPageSize().getHeight();
            try {
                width = sizeFormatter.parse(widthTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                height = sizeFormatter.parse(heightTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }

            if (millimeter) {
                width *= MM;
                height *= MM;
            } else {
                width *= INCH;
                height *= INCH;
            }
            float w = (float) width;
            float h = (float) height;
            Rectangle rect = new Rectangle(w, h);
            pdfExporter.setPageSize(rect);
        } else {
            pdfExporter.setPageSize(((PageSizeItem) pageSizeCombo.getSelectedItem()).getPageSize());
        }

        pdfExporter.setLandscape(landscapeRadio.isSelected());

        double top = pdfExporter.getMarginTop();
        double bottom = pdfExporter.getMarginBottom();
        double left = pdfExporter.getMarginLeft();
        double right = pdfExporter.getMarginRight();
        try {
            top = marginFormatter.parse(topMarginTextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        try {
            bottom = marginFormatter.parse(bottomMarginTextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        try {
            left = marginFormatter.parse(leftMarginTextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        try {
            right = marginFormatter.parse(rightMargintextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        if (millimeter) {
            top *= MM;
            bottom *= MM;
            left *= MM;
            right *= MM;
        }
        pdfExporter.setMarginTop((float) top);
        pdfExporter.setMarginBottom((float) bottom);
        pdfExporter.setMarginLeft((float) left);
        pdfExporter.setMarginRight((float) right);

        savePreferences();
    }

    private void updatePageSize() {
        if (pageSizeCombo.getSelectedItem() != customSizeString && !widthTextField.getText().isEmpty() && !heightTextField.getText().isEmpty()) {
            DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) pageSizeCombo.getModel();
            PageSizeItem item = getItem(widthTextField.getText(), heightTextField.getText());
            if (item == null) {
                comboBoxModel.setSelectedItem(customSizeString);
            } else {
                comboBoxModel.setSelectedItem(item);
            }
        }
    }

    private void setPageSize(PageSizeItem pageSize) {
        double pageWidth = 0;
        double pageHeight = 0;
        if (millimeter) {
            pageWidth = pageSize.mmWidth;
            pageHeight = pageSize.mmHeight;
        } else {
            pageWidth = pageSize.inWidth;
            pageHeight = pageSize.inHeight;
        }
        widthTextField.setText(sizeFormatter.format(pageWidth));
        heightTextField.setText(sizeFormatter.format(pageHeight));
    }

    private void setOrientation(boolean landscape) {
        portraitRadio.setSelected(!landscape);
        landscapeRadio.setSelected(landscape);
    }

    private void setMargins(float top, float bottom, float left, float right) {
        if (millimeter) {
            top /= MM;
            bottom /= MM;
            left /= MM;
            right /= MM;
        }
        topMarginTextField.setText(marginFormatter.format(top));
        bottomMarginTextField.setText(marginFormatter.format(bottom));
        leftMarginTextField.setText(marginFormatter.format(left));
        rightMargintextField.setText(marginFormatter.format(right));
    }

    private PageSizeItem getItem(String width, String height) {
        DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) pageSizeCombo.getModel();
        for (int i = 0; i < comboBoxModel.getSize(); i++) {
            Object o = comboBoxModel.getElementAt(i);
            if (o instanceof PageSizeItem) {
                PageSizeItem pageSize = (PageSizeItem) o;
                double pageWidth = 0;
                double pageHeight = 0;
                if (millimeter) {
                    pageWidth = pageSize.mmWidth;
                    pageHeight = pageSize.mmHeight;
                } else {
                    pageWidth = pageSize.inWidth;
                    pageHeight = pageSize.inHeight;
                }
                String wStr = sizeFormatter.format(pageWidth);
                String hStr = sizeFormatter.format(pageHeight);
                if (wStr.equals(width) && hStr.equals(height)) {
                    return ((PageSizeItem) o);
                }
            }
        }
        return null;
    }

    private void refreshUnit(boolean convert) {
        unitLink.setText(millimeter ? "Milimeter" : "Inch");
        widthUnitLabel.setText(millimeter ? "mm" : "in");
        heightUnitLabel.setText(millimeter ? "mm" : "in");
        if (convert) {
            if (pageSizeCombo.getSelectedItem() != customSizeString) {
                setPageSize((PageSizeItem) pageSizeCombo.getSelectedItem());
            } else {
                double width = 0;
                double height = 0;
                try {
                    width = sizeFormatter.parse(widthTextField.getText()).doubleValue();
                } catch (ParseException ex) {
                }
                try {
                    height = sizeFormatter.parse(heightTextField.getText()).doubleValue();
                } catch (ParseException ex) {
                }
                
                if (!millimeter) {
                    width *= MM / INCH;
                    height *= MM / INCH;
                } else {
                    width *= INCH / MM;
                    height *= INCH / MM;
                }
                widthTextField.setText(sizeFormatter.format(width));
                heightTextField.setText(sizeFormatter.format(height));
            }
            updatePageSize();
            double top = 0.;
            double bottom = 0.;
            double left = 0.;
            double right = 0.;
            try {
                top = marginFormatter.parse(topMarginTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                bottom = marginFormatter.parse(bottomMarginTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                left = marginFormatter.parse(leftMarginTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                right = marginFormatter.parse(rightMargintextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            if (!millimeter) {
                top *= MM / INCH;
                bottom *= MM / INCH;
                left *= MM / INCH;
                right *= MM / INCH;
            } else {
                top *= INCH / MM;
                bottom *= INCH / MM;
                left *= INCH / MM;
                right *= INCH / MM;
            }
            topMarginTextField.setText(marginFormatter.format(top));
            bottomMarginTextField.setText(marginFormatter.format(bottom));
            leftMarginTextField.setText(marginFormatter.format(left));
            rightMargintextField.setText(marginFormatter.format(right));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orientationButtonGroup = new javax.swing.ButtonGroup();
        labelPageSize = new javax.swing.JLabel();
        pageSizeCombo = new javax.swing.JComboBox();
        labelWidth = new javax.swing.JLabel();
        widthTextField = new javax.swing.JTextField();
        labelHeight = new javax.swing.JLabel();
        heightTextField = new javax.swing.JTextField();
        widthUnitLabel = new javax.swing.JLabel();
        heightUnitLabel = new javax.swing.JLabel();
        labelOrientation = new javax.swing.JLabel();
        portraitRadio = new javax.swing.JRadioButton();
        landscapeRadio = new javax.swing.JRadioButton();
        labelMargins = new javax.swing.JLabel();
        labelTop = new javax.swing.JLabel();
        topMarginTextField = new javax.swing.JTextField();
        labelBottom = new javax.swing.JLabel();
        bottomMarginTextField = new javax.swing.JTextField();
        labelLeft = new javax.swing.JLabel();
        labelRight = new javax.swing.JLabel();
        leftMarginTextField = new javax.swing.JTextField();
        rightMargintextField = new javax.swing.JTextField();
        labelUnit = new javax.swing.JLabel();
        unitLink = new org.jdesktop.swingx.JXHyperlink();

        labelPageSize.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelPageSize.text")); // NOI18N

        pageSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        labelWidth.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelWidth.text")); // NOI18N

        widthTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        widthTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.widthTextField.text")); // NOI18N

        labelHeight.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelHeight.text")); // NOI18N

        heightTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        heightTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.heightTextField.text")); // NOI18N

        widthUnitLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.widthUnitLabel.text")); // NOI18N

        heightUnitLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.heightUnitLabel.text")); // NOI18N

        labelOrientation.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelOrientation.text")); // NOI18N

        orientationButtonGroup.add(portraitRadio);
        portraitRadio.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.portraitRadio.text")); // NOI18N

        orientationButtonGroup.add(landscapeRadio);
        landscapeRadio.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.landscapeRadio.text")); // NOI18N

        labelMargins.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelMargins.text")); // NOI18N

        labelTop.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelTop.text")); // NOI18N

        topMarginTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.topMarginTextField.text")); // NOI18N

        labelBottom.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelBottom.text")); // NOI18N

        bottomMarginTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.bottomMarginTextField.text")); // NOI18N

        labelLeft.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelLeft.text")); // NOI18N

        labelRight.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelRight.text")); // NOI18N

        leftMarginTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.leftMarginTextField.text")); // NOI18N

        rightMargintextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.rightMargintextField.text")); // NOI18N

        labelUnit.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelUnit.text")); // NOI18N

        unitLink.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.unitLink.text")); // NOI18N
        unitLink.setToolTipText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.unitLink.toolTipText")); // NOI18N
        unitLink.setFocusPainted(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelUnit)
                        .addGap(62, 62, 62)
                        .addComponent(unitLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelPageSize)
                            .addComponent(labelOrientation)
                            .addComponent(labelMargins))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(landscapeRadio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(portraitRadio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(labelHeight)
                                            .addComponent(labelWidth))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(heightTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(widthUnitLabel)
                                    .addComponent(heightUnitLabel)))
                            .addComponent(pageSizeCombo, 0, 224, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelTop)
                                        .addGap(26, 26, 26)
                                        .addComponent(topMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(labelLeft))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelBottom)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bottomMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(labelRight)))
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(leftMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(rightMargintextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelUnit)
                    .addComponent(unitLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPageSize)
                    .addComponent(pageSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthUnitLabel)
                    .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelWidth))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightUnitLabel)
                    .addComponent(labelHeight))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelOrientation)
                    .addComponent(portraitRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(landscapeRadio)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMargins)
                    .addComponent(labelTop)
                    .addComponent(topMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelLeft)
                    .addComponent(leftMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bottomMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelBottom)
                    .addComponent(labelRight)
                    .addComponent(rightMargintextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private static class PageSizeItem {

        private final Rectangle pageSize;
        private String name = "";
        private final double inWidth;
        private final double inHeight;
        private final double mmWidth;
        private final double mmHeight;

        public PageSizeItem(Rectangle pageSize) {
            this.pageSize = pageSize;
            this.inHeight = pageSize.getHeight() / INCH;
            this.inWidth = pageSize.getWidth() / INCH;
            this.mmHeight = pageSize.getHeight() / MM;
            this.mmWidth = pageSize.getWidth() / MM;
        }

        public PageSizeItem(Rectangle pageSize, String name, double mmWidth, double mmHeight, double inWidth, double inHeight) {
            this.pageSize = pageSize;
            this.name = name;
            this.inHeight = inHeight;
            this.inWidth = inWidth;
            this.mmHeight = mmHeight;
            this.mmWidth = mmWidth;
        }

        public Rectangle getPageSize() {
            return pageSize;
        }

        public double getInHeight() {
            return inHeight;
        }

        public double getInWidth() {
            return inWidth;
        }

        public double getMmHeight() {
            return mmHeight;
        }

        public double getMmWidth() {
            return mmWidth;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PageSizeItem other = (PageSizeItem) obj;
            if (this.pageSize != other.pageSize && (this.pageSize == null || !this.pageSize.equals(other.pageSize))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + (this.pageSize != null ? this.pageSize.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class PositiveSizeValidator implements Validator<String> {

        private UIExporterPDFPanel panel;

        public PositiveSizeValidator(UIExporterPDFPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            boolean result = false;
            try {
                double i = panel.sizeFormatter.parse(panel.widthTextField.getText()).doubleValue();
                result = i > 0;
            } catch (ParseException ex) {
            }
            if (!result) {
                String message = NbBundle.getMessage(getClass(),
                        "PositiveSizeValidator.NEGATIVE", model);
                problems.add(message);
            }
            return result;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bottomMarginTextField;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JLabel heightUnitLabel;
    private javax.swing.JLabel labelBottom;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelLeft;
    private javax.swing.JLabel labelMargins;
    private javax.swing.JLabel labelOrientation;
    private javax.swing.JLabel labelPageSize;
    private javax.swing.JLabel labelRight;
    private javax.swing.JLabel labelTop;
    private javax.swing.JLabel labelUnit;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JRadioButton landscapeRadio;
    private javax.swing.JTextField leftMarginTextField;
    private javax.swing.ButtonGroup orientationButtonGroup;
    private javax.swing.JComboBox pageSizeCombo;
    private javax.swing.JRadioButton portraitRadio;
    private javax.swing.JTextField rightMargintextField;
    private javax.swing.JTextField topMarginTextField;
    private org.jdesktop.swingx.JXHyperlink unitLink;
    private javax.swing.JTextField widthTextField;
    private javax.swing.JLabel widthUnitLabel;
    // End of variables declaration//GEN-END:variables
}
