package org.gephi.ui.generator.plugin;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.lib.validation.PositiveNumberValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.util.NbBundle;

public class ScaleFreeGraphPanel extends JPanel {

    protected final JTextField nodeField = new JTextField();
    protected final JTextField edgeField = new JTextField();
    protected final JTextField seedField = new JTextField();
    private final JLabel nodeLabel = new JLabel();
    private final JLabel edgeLabel = new JLabel();
    private final JLabel seedLabel = new JLabel();
    private DocumentListener revalidateListener;

    public ScaleFreeGraphPanel() {
        nodeLabel.setText(NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.nodeLabel.text"));
        edgeLabel.setText(NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.edgeLabel.text"));
        seedLabel.setText(NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.seedLabel.text"));
        seedField.setToolTipText(NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.seedField.tooltip"));
        nodeLabel.setLabelFor(nodeField);
        edgeLabel.setLabelFor(edgeField);
        seedLabel.setLabelFor(seedField);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(nodeLabel)
                    .addComponent(edgeLabel)
                    .addComponent(seedLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(nodeField, GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(edgeField)
                    .addComponent(seedField))
                .addContainerGap(19, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodeLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addComponent(edgeLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(seedField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addComponent(seedLabel))
                .addContainerGap(28, Short.MAX_VALUE)
        );
    }

    public static ValidationPanel createValidationPanel(ScaleFreeGraphPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            innerPanel = new ScaleFreeGraphPanel();
        }
        validationPanel.setInnerComponent(innerPanel);
        ValidationGroup group = validationPanel.getValidationGroup();

        //Make sure components have names
        innerPanel.nodeField.setName(innerPanel.nodeLabel.getText().replace(":", ""));
        innerPanel.edgeField.setName(innerPanel.edgeLabel.getText().replace(":", ""));
        innerPanel.seedField.setName(innerPanel.seedLabel.getText().replace(":", ""));

        final JTextField nodeField = innerPanel.nodeField;

        //Node field, the model needs at least a node to attach to
        group.add(nodeField, ValidatorUtils.merge(StringValidators.REQUIRE_NON_EMPTY_STRING,
            new PositiveNumberValidator(), new Validator<String>() {
                @Override
                public void validate(Problems problems, String compName, String model) {
                    try {
                        if (Integer.parseInt(model) < 2) {
                            problems.add(NbBundle.getMessage(ScaleFreeGraphPanel.class,
                                "ScaleFreeGraphPanel.nodes.tooSmall", compName));
                        }
                    } catch (NumberFormatException e) {
                        //Reported by the other validators
                    }
                }

                @Override
                public Class<String> modelType() {
                    return String.class;
                }
            }));

        //Edge field, each new node needs that many distinct nodes to attach to
        group.add(innerPanel.edgeField, ValidatorUtils.merge(StringValidators.REQUIRE_NON_EMPTY_STRING,
            new PositiveNumberValidator(), new Validator<String>() {
                @Override
                public void validate(Problems problems, String compName, String model) {
                    try {
                        int edges = Integer.parseInt(model);
                        int nodes = Integer.parseInt(nodeField.getText());
                        if (edges >= nodes) {
                            problems.add(NbBundle.getMessage(ScaleFreeGraphPanel.class,
                                "ScaleFreeGraphPanel.edges.tooLarge", compName));
                        }
                    } catch (NumberFormatException e) {
                        //Reported by the other validators
                    }
                }

                @Override
                public Class<String> modelType() {
                    return String.class;
                }
            }));

        //Seed field, optional
        group.add(innerPanel.seedField, new Validator<String>() {
            @Override
            public void validate(Problems problems, String compName, String model) {
                if (model.trim().isEmpty()) {
                    return;
                }
                try {
                    Long.parseLong(model.trim());
                } catch (NumberFormatException e) {
                    problems.add(NbBundle.getMessage(ScaleFreeGraphPanel.class,
                        "ScaleFreeGraphPanel.seed.invalid", compName));
                }
            }

            @Override
            public Class<String> modelType() {
                return String.class;
            }
        });

        // The edge field depends on the node field, validators only run on the field being edited
        if (innerPanel.revalidateListener != null) {
            nodeField.getDocument().removeDocumentListener(innerPanel.revalidateListener);
        }
        innerPanel.revalidateListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                group.performValidation();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                group.performValidation();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                group.performValidation();
            }
        };
        nodeField.getDocument().addDocumentListener(innerPanel.revalidateListener);

        return validationPanel;
    }
}
