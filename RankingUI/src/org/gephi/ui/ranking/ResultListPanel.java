/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.ui.ranking;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.ColorTransformer;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.RankingResult;
import org.gephi.ranking.api.SizeTransformer;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.ui.utils.UIUtils;
import org.jdesktop.swingx.JXTable;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class ResultListPanel extends JScrollPane implements LookupListener {

    //Const
    private final String LAST_PATH = "ResultListPanel_TableScreenshot_Last_Path";
    private final String LAST_PATH_DEFAULT = "ResultListPanel_TableScreenshot_Last_Path_Default";

    //Variable
    private Lookup.Result<RankingResult> result;
    private JXTable table;
    private JPopupMenu popupMenu;

    public ResultListPanel() {
        //Lookup listener
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        Lookup eventBus = rankingController.getEventBus();
        result = eventBus.lookupResult(RankingResult.class);
        result.addLookupListener(this);

        initTable();
        initTablePopup();
    }

    public void resultChanged(LookupEvent ev) {
        Lookup.Result<RankingResult> r = (Lookup.Result<RankingResult>) ev.getSource();
        RankingResult[] res = r.allInstances().toArray(new RankingResult[0]);
        if (res.length > 0) {
            RankingResult lastResult = res[0];
            if (isVisible()) {
                fetchTable(lastResult);
            }
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

    private void fetchTable(RankingResult result) {
        List<RankingResult.RankingResultLine> results = new ArrayList<RankingResult.RankingResultLine>();
        results.addAll(result.getResultLines());
        Collections.reverse(results);
        boolean nodeRanking = result.getRanking() instanceof NodeRanking;
        List<RankCell> cells = new ArrayList<RankCell>();
        List<String> labels = new ArrayList<String>();
        for (RankingResult.RankingResultLine line : results) {
            if (line.getResult() != null) {
                if (result.getTransformer() instanceof ColorTransformer) {
                    RankCellColor rankCellColor = new RankCellColor((Color) line.getResult(), line.getRank());
                    cells.add(rankCellColor);
                } else if (result.getTransformer() instanceof SizeTransformer) {
                    RankCellSize rankCellSize = new RankCellSize((Float) line.getResult(), line.getRank());
                    cells.add(rankCellSize);
                } else {
                }
                if (nodeRanking) {
                    Node n = (Node) line.getTarget();
                    labels.add(n.getNodeData().getLabel());
                } else {
                    Edge e = (Edge) line.getTarget();
                    labels.add(e.getEdgeData().getLabel());
                }
            }
        }

        ResultListTableModel model = new ResultListTableModel(cells.toArray(new RankCell[0]), labels.toArray(new String[0]));
        table.setDefaultRenderer(RankCell.class, new RankCellRenderer());
        table.setModel(model);
    }

    private class ResultListTableModel implements TableModel {

        private RankCell[] ranks;
        private String[] labels;

        public ResultListTableModel(RankCell[] ranks, String[] labels) {
            this.ranks = ranks;
            this.labels = labels;
        }

        public int getRowCount() {
            return labels.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "Rank";
            } else {
                return "Label";
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
                return labels[rowIndex];
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                ranks[rowIndex] = (RankCell) aValue;
            } else {
                labels[rowIndex] = (String) aValue;
            }
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public void removeTableModelListener(TableModelListener l) {
        }
    }

    private static interface RankCell {

        public void render(JLabel label);
    }

    private static class RankCellColor implements RankCell {

        private Color color;
        private Object rank;

        public RankCellColor(Color color, Object rank) {
            this.color = color;
            this.rank = rank;
        }

        public void render(JLabel label) {
            label.setBackground(color);
            label.setForeground(UIUtils.getForegroundColorForBackground(color));
            label.setText(rank.toString());
        }
    }

    private static class RankCellSize implements RankCell {

        private Float size;
        private Object rank;

        public RankCellSize(Float size, Object rank) {
            this.size = size;
            this.rank = rank;
        }

        public void render(JLabel label) {
            label.setText(rank.toString());
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
