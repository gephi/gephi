package org.gephi.desktop.attributes.selection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.ZoneId;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import org.gephi.desktop.attributes.AttributesUIModelImpl;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeFormat;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class SelectionPanel extends JPanel {

    private static final int CIRCLE_SIZE = 14;
    private static final int MAX_VALUE_LENGTH = 100;

    private final JPanel headerPanel;
    private final JLabel headerLabel;
    private final JPanel attributesPanel;
    private final JLabel noSelectionLabel;

    public SelectionPanel() {
        setLayout(new BorderLayout());

        headerPanel = new JPanel(new BorderLayout());
        headerLabel = new JLabel();
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 14f));
        headerLabel.setIconTextGap(8);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(new JSeparator(), BorderLayout.SOUTH);
        headerPanel.setVisible(false);

        add(headerPanel, BorderLayout.NORTH);

        attributesPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(attributesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        noSelectionLabel = new JLabel(
            NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.noSelection"));
        noSelectionLabel.setForeground(UIManager.getColor("textInactiveText"));
        noSelectionLabel.setHorizontalAlignment(JLabel.CENTER);
        emptySelection();
    }

    public void refreshSelectedNodes(final AttributesUIModelImpl model) {
        boolean showNullColumns = model.isShowNullColumns();
        Node[] nodes = model.getSelectedNodes();
        if (nodes == null || nodes.length == 0) {
            emptySelection();
            return;
        }

        Node node = nodes[nodes.length - 1];
        Color nodeColor = node.getColor();
        String label = node.getLabel();
        if (label == null) {
            label = node.getId().toString();
        }

        Color opaqueColor = new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue());
        headerLabel.setIcon(new CircleIcon(opaqueColor, CIRCLE_SIZE));
        headerLabel.setText(label);
        headerPanel.setVisible(true);

        attributesPanel.removeAll();

        GraphModel graphModel = model.getGraphModel();
        TimeFormat timeFormat = graphModel.getTimeFormat();
        ZoneId timeZone = graphModel.getTimeZone();
        Graph graph = graphModel.getGraphVisible();
        Column degreeColumn = graphModel.defaultColumns().degree();
        Column inDegreeColumn = graphModel.defaultColumns().inDegree();
        Column outDegreeColumn = graphModel.defaultColumns().outDegree();

        List<Column> selectedColumns = model.getSelectedColumns();
        if (selectedColumns.isEmpty()) {
            JLabel noColumnsLabel = new JLabel(
                NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.noColumns"));
            noColumnsLabel.setForeground(UIManager.getColor("textInactiveText"));
            noColumnsLabel.setHorizontalAlignment(JLabel.CENTER);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.CENTER;
            attributesPanel.add(noColumnsLabel, gbc);
        } else {
            String nullColumnLabel = NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.nullColumn");
            int row = 0;
            for (Column column : selectedColumns) {
                String columnName = column.getTitle();
                String valueStr = null;
                Object value = null;
                if (column == degreeColumn) {
                    value = graph.getDegree(node);
                    valueStr = String.valueOf(value);
                } else if (column == inDegreeColumn) {
                    value = ((DirectedGraph) graph).getInDegree(node);
                    valueStr = String.valueOf(value);
                } else if (column == outDegreeColumn) {
                    value = ((DirectedGraph) graph).getOutDegree(node);
                    valueStr = String.valueOf(value);
                } else {
                    value = node.getAttribute(column, graph.getView());
                    if (value == null && !showNullColumns) {
                        continue;
                    }

                    valueStr =
                        value == null ? nullColumnLabel : AttributeUtils.print(value, timeFormat, timeZone);
                }

                Icon icon = getTypeIcon(column);
                addAttributeRow(row++, icon, columnName, valueStr, value == null);
            }

            GridBagConstraints filler = new GridBagConstraints();
            filler.gridx = 0;
            filler.gridy = row;
            filler.weighty = 1.0;
            attributesPanel.add(Box.createVerticalGlue(), filler);
        }

        revalidate();
        repaint();
    }

    private void addAttributeRow(int row, Icon icon, String name, String value, boolean isNull) {
        GridBagConstraints gbc;

        JLabel iconLabel = new JLabel(icon);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 4, 4, 4);
        attributesPanel.add(iconLabel, gbc);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 8);
        attributesPanel.add(nameLabel, gbc);

        String displayValue = value;
        String tooltip = null;
        if (value.length() > MAX_VALUE_LENGTH) {
            displayValue = value.substring(0, MAX_VALUE_LENGTH) + "\u2026";
            tooltip = "<html><body style='width:300px'>" + escapeHtml(value) + "</body></html>";
        }

        JLabel valueLabel = new JLabel(displayValue);
        if (isNull) {
            valueLabel.setForeground(UIManager.getColor("textInactiveText"));
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.ITALIC));
        }
        if (tooltip != null) {
            valueLabel.setToolTipText(tooltip);
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 10);
        attributesPanel.add(valueLabel, gbc);
    }

    private Icon getTypeIcon(Column column) {
        if (column.isDynamicAttribute() || column.isDynamic()) {
            return ImageUtilities.loadImageIcon("DesktopAttributes/dynamic.svg", false);
        } else if (column.isNumber()) {
            return ImageUtilities.loadImageIcon("DesktopAttributes/number.svg", false);
        } else if (column.isArray()) {
            return ImageUtilities.loadImageIcon("DesktopAttributes/array.svg", false);
        } else if (column.getTypeClass() == Boolean.class) {
            return ImageUtilities.loadImageIcon("DesktopAttributes/boolean.svg", false);
        }
        return ImageUtilities.loadImageIcon("DesktopAttributes/string.svg", false);
    }

    private void emptySelection() {
        headerPanel.setVisible(false);
        attributesPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        attributesPanel.add(noSelectionLabel, gbc);

        revalidate();
        repaint();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static class CircleIcon implements Icon {
        private final Color color;
        private final int size;

        CircleIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
