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
package org.gephi.desktop.ranking;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.ranking.api.Transformer;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.ui.utils.UIUtils;
import org.jdesktop.swingx.JXTable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class ResultListPanel extends JScrollPane {

    //Const
    private final String LAST_PATH = "ResultListPanel_TableScreenshot_Last_Path";
    private final String LAST_PATH_DEFAULT = "ResultListPanel_TableScreenshot_Last_Path_Default";
    //Variable
    private JXTable table;
    private JPopupMenu popupMenu;
    private RankingUIModel model;
    private PropertyChangeListener rankingUiModelListener;

    public ResultListPanel() {
        initTable();
        initTablePopup();
    }

    public void select(RankingUIModel model) {
        this.model = model;

        refreshTable();

        rankingUiModelListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals(RankingUIModel.APPLY_TRANSFORMER)) {
                    refreshTable();
                }
            }
        };
        model.addPropertyChangeListener(rankingUiModelListener);
    }

    public void unselect() {
        if (model != null) {
            if (rankingUiModelListener != null) {
                model.removePropertyChangeListener(rankingUiModelListener);
            }
        }
        rankingUiModelListener = null;
        model = null;
    }

    private void refreshTable() {
        Ranking ranking = model.getCurrentRanking();
        Transformer transformer = model.getCurrentTransformer();

        if (model.isListVisible()) {
            fetchTable(ranking, transformer);
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    table.setModel(new ResultListTableModel(new RankCell[0]));
                }
            });
        }
    }

    private void initTable() {
        table = new JXTable();
        table.putClientProperty(JXTable.USE_DTCR_COLORMEMORY_HACK, Boolean.FALSE);      //Disable strange hack that overwrite JLabel's setBackground() by a Highlighter color
        table.setColumnControlVisible(false);
        table.setEditable(false);
        table.setSortable(false);
        table.setRolloverEnabled(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        //table.setHighlighters(HighlighterFactory.createAlternateStriping());

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);

        setViewportView(table);
    }

    private void initTablePopup() {
        popupMenu = new JPopupMenu();
        JMenuItem screenshotItem = new JMenuItem(NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.action.tablescreenshot"));
        screenshotItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage image = UIUtils.createComponentScreenshot(table);
                    writeImage(image);
                } catch (Exception ex) {
                    String msg = NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.tablescreenshot.error", new Object[]{ex.getClass().getSimpleName(), ex.getLocalizedMessage(), ex.getStackTrace()[0].getClassName(), ex.getStackTrace()[0].getLineNumber()});
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.tablescreenshot.error.title"), JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        popupMenu.add(screenshotItem);

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void writeImage(BufferedImage image) throws Exception {
        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ResultListPanel.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ResultListPanel.class).get(LAST_PATH, lastPathDefault);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle(NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.tablescreenshot.filechooser.title"));
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.tablescreenshot.filechooser.pngDescription"));
        dialogFileFilter.addExtension("png");
        chooser.addChoosableFileFilter(dialogFileFilter);
        File selectedFile = new File(chooser.getCurrentDirectory(), "rank_table.png");
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }
        selectedFile = chooser.getSelectedFile();

        if (!selectedFile.getPath().endsWith(".png")) {
            selectedFile = new File(selectedFile.getPath() + ".png");
        }

        //Save last path
        String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
        NbPreferences.forModule(ResultListPanel.class).put(LAST_PATH, defaultDirectory);


        String format = "png";
        if (!ImageIO.write(image, format, selectedFile)) {
            throw new IOException("Unsupported file format");
        }
    }

    private void fetchTable(Ranking ranking, Transformer transformer) {
        final List<RankCell> cells = new ArrayList<RankCell>();

        if (ranking.getElementType().equals(Ranking.NODE_ELEMENT)) {
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            Graph graph = graphController.getModel().getGraphVisible();
            for (Node n : graph.getNodes()) {
                Number rank = ranking.getValue(n);
                if (transformer instanceof AbstractColorTransformer) {
                    Color c = new Color(n.getNodeData().r(), n.getNodeData().g(), n.getNodeData().b());
                    RankCellColor rankCellColor = new RankCellColor(c, rank, n.getNodeData().getLabel());
                    cells.add(rankCellColor);
                } else if (transformer instanceof AbstractSizeTransformer) {
                    float size = n.getNodeData().getSize();
                    RankCellSize rankCellSize = new RankCellSize(size, rank, n.getNodeData().getLabel());
                    cells.add(rankCellSize);
                }
            }

        } else if (ranking.getElementType().equals(Ranking.EDGE_ELEMENT)) {
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            HierarchicalGraph graph = graphController.getModel().getHierarchicalGraphVisible();
            for (Edge e : graph.getEdgesAndMetaEdges()) {
                Number rank = ranking.getValue(e);
                if (transformer instanceof AbstractColorTransformer) {
                    Color c = new Color(e.getEdgeData().r(), e.getEdgeData().g(), e.getEdgeData().b());
                    RankCellColor rankCellColor = new RankCellColor(c, rank, e.getEdgeData().getLabel());
                    cells.add(rankCellColor);
                } else if (transformer instanceof AbstractSizeTransformer) {
                    float size = e.getEdgeData().getSize();
                    RankCellSize rankCellSize = new RankCellSize(size, rank, e.getEdgeData().getLabel());
                    cells.add(rankCellSize);
                }
            }
        }

        Collections.sort(cells);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ResultListTableModel m = new ResultListTableModel(cells.toArray(new RankCell[0]));
                table.setDefaultRenderer(RankCell.class, new RankCellRenderer());
                TableRowSorter tableRowSorter = new TableRowSorter(m);
                tableRowSorter.setComparator(0, new Comparator<RankCell>() {

                    public int compare(RankCell t, RankCell t1) {
                        return t.compareTo(t1);
                    }
                });
                List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
                sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
                tableRowSorter.setSortKeys(sortKeys);

                table.setRowSorter(tableRowSorter);
                table.setModel(m);
            }
        });
    }

    private class ResultListTableModel implements TableModel {

        private RankCell[] ranks;

        public ResultListTableModel(RankCell[] ranks) {
            this.ranks = ranks;
        }

        public int getRowCount() {
            return ranks.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.column.rank");
            } else {
                return NbBundle.getMessage(ResultListPanel.class, "ResultListPanel.column.label");
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return RankCell.class;
            } else {
                return String.class;
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return ranks[rowIndex];
            } else {
                return ranks[rowIndex].getLabel();
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                ranks[rowIndex] = (RankCell) aValue;
            } else {
                ranks[rowIndex].setLabel((String) aValue);
            }
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public void removeTableModelListener(TableModelListener l) {
        }
    }

    private static interface RankCell extends Comparable<RankCell> {

        public void render(JLabel label);

        public String getLabel();

        public void setLabel(String label);
    }

    private static class RankCellColor implements RankCell {

        private final Color color;
        private final Number rank;
        private String label;

        public RankCellColor(Color color, Number rank, String label) {
            this.color = color;
            this.rank = rank;
            this.label = label;
        }

        public void render(JLabel label) {
            label.setBackground(color);
            label.setForeground(UIUtils.getForegroundColorForBackground(color));
            label.setText(rank.toString());
        }

        public int compareTo(RankCell t) {
            double d2 = rank.doubleValue();
            double d1 = ((RankCellColor) t).rank.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            }
            return 0;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    private static class RankCellSize implements RankCell {

        private final Float size;
        private final Number rank;
        private String label;

        public RankCellSize(Float size, Number rank, String label) {
            this.size = size;
            this.rank = rank;
            this.label = label;
        }

        public void render(JLabel label) {
            label.setText(rank.toString());
        }

        public int compareTo(RankCell t) {
            double d2 = rank.doubleValue();
            double d1 = ((RankCellSize) t).rank.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            }
            return 0;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    private static class RankCellRenderer extends DefaultTableCellRenderer {

        public RankCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            RankCell rankCell = (RankCell) value;
            rankCell.render(this);
            return this;
        }
    }
}
