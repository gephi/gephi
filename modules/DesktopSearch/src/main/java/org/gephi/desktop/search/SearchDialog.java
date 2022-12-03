package org.gephi.desktop.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.desktop.search.api.SearchController;
import org.gephi.desktop.search.api.SearchListener;
import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.api.SearchResult;
import org.gephi.desktop.search.popup.ActionPopup;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author mathieu.bastian
 */
public class SearchDialog extends javax.swing.JPanel implements SearchListener {

    private final SearchUIModel uiModel;
    private final ButtonGroup categoryGroup;

    public SearchDialog(SearchUIModel uiModel) {
        this.uiModel = uiModel;
        categoryGroup = new ButtonGroup();
        initComponents();
        setup();
    }

    protected void setup() {
        // Tabs
        allCategoriesButton.addActionListener(e -> {
            uiModel.setCategory(null);
            search();
        });
        allCategoriesButton.putClientProperty("JButton.buttonType", "square");
        allCategoriesButton.setSelected(true);
        categoryGroup.add(allCategoriesButton);
        uiModel.getCategories().forEach(category -> {
            JToggleButton toggleButton = new JToggleButton();
            toggleButton.setText(category.getDisplayName());
            toggleButton.setFocusable(false);
            toggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            toggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toggleButton.putClientProperty("JButton.buttonType", "square");
            categoryToolbar.add(toggleButton);
            categoryGroup.add(toggleButton);
            toggleButton.addActionListener(e -> {
                uiModel.setCategory(category);
                search();
            });
            if (uiModel.category == category) {
                toggleButton.setSelected(true);
            }
        });

        // Results list
        resultsList.setFocusable(false);
        resultsList.setCellRenderer(new ResultRenderer());
        resultsList.addMouseListener(new ActionPopup(resultsList));
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    click();
                }
            }
        });
        resultsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    select();
                }
            }
        });

        // Search
        searchField.setText(uiModel.query);
        searchField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            search();
        });
        searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    resultsList.setSelectedIndex(Math.max(0, resultsList.getSelectedIndex() - 1));
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    resultsList.setSelectedIndex(resultsList.getSelectedIndex() + 1);
                } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    click();
                }
            }
        });

        // Search if query isn't empty
        if (!uiModel.query.isEmpty()) {
            search();
        }

        // Focus
        searchField.requestFocusInWindow();
    }

    protected void unsetup() {
        resetSelection();
    }

    protected void search() {
        String query = searchField.getText();
        uiModel.query = query;
        if (query != null && !query.trim().isEmpty()) {
            SearchRequest request = SearchRequest.builder().query(query.trim()).category(uiModel.category).build();
            SearchController searchController = Lookup.getDefault().lookup(SearchController.class);
            searchController.search(request, this);
        } else {
            resultsList.setModel(new ResultsListModel(Collections.emptyList()));
        }
    }

    protected void click() {
        SearchResult result = resultsList.getSelectedValue();
        if (result != null) {
            Object val = result.getResult();
            if (isGraphOpened()) {
                VisualizationController visualizationController =
                    Lookup.getDefault().lookup(VisualizationController.class);
                if (visualizationController != null) {
                    if (val instanceof Node) {
                        visualizationController.centerOnNode((Node) val);
                    } else if (val instanceof Edge) {
                        visualizationController.centerOnEdge((Edge) val);
                    }
                }
            }
            if (isDataLabOpened()) {
                DataTablesController dataTablesController = Lookup.getDefault().lookup(DataTablesController.class);
                if (dataTablesController != null) {
                    if (val instanceof Node) {
                        dataTablesController.selectNodesTable();
                        dataTablesController.setNodeTableSelection(new Node[] {(Node) val});
                    } else if (val instanceof Edge) {
                        dataTablesController.selectEdgesTable();
                        dataTablesController.setEdgeTableSelection(new Edge[] {(Edge) val});
                    }
                }
            }
        }
    }

    protected void select() {
        SearchResult result = resultsList.getSelectedValue();
        if (result != null) {
            Object val = result.getResult();
            if (isGraphOpened()) {
                VisualizationController visualizationController =
                    Lookup.getDefault().lookup(VisualizationController.class);
                if (visualizationController != null) {
                    if (val instanceof Node) {
                        visualizationController.resetSelection();
                        visualizationController.selectNodes(new Node[] {(Node) val});
                    } else if (val instanceof Edge) {
                        visualizationController.resetSelection();
                        visualizationController.selectEdges(new Edge[] {(Edge) val});
                    }
                }
            }
            if (isDataLabOpened()) {
                DataTablesController dataTablesController = Lookup.getDefault().lookup(DataTablesController.class);
                if (dataTablesController != null) {
                    if (val instanceof Node && dataTablesController.isNodeTableMode()) {
                        dataTablesController.setNodeTableSelection(new Node[] {(Node) val});
                    } else if (val instanceof Edge && dataTablesController.isEdgeTableMode()) {
                        dataTablesController.setEdgeTableSelection(new Edge[] {(Edge) val});
                    }
                }
            }
        } else {
            resetSelection();
        }
    }

    private void resetSelection() {
        if (isGraphOpened()) {
            VisualizationController visualizationController =
                Lookup.getDefault().lookup(VisualizationController.class);
            if (visualizationController != null) {
                visualizationController.resetSelection();
            }
        }
        if (isDataLabOpened()) {
            DataTablesController dataTablesController = Lookup.getDefault().lookup(DataTablesController.class);
            if (dataTablesController != null) {
                dataTablesController.clearSelection();
            }
        }
    }

    protected void instrumentDragListener(JDialog dialog) {
        DragListener dragListener = new DragListener(dialog);
        categoryToolbar.addMouseListener(dragListener);
        categoryToolbar.addMouseMotionListener(dragListener);
    }

    @Override
    public void started(SearchRequest request) {

    }

    @Override
    public void cancelled() {

    }

    @Override
    public void finished(SearchRequest request, List<SearchResult> results) {
        SwingUtilities.invokeLater(() -> {
            resultsList.setModel(new ResultsListModel(results));
            resultsList.setSelectedIndex(0);
        });
    }

    private static class ResultRenderer implements ListCellRenderer<SearchResult> {

        protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        // Icons
        private final ImageIcon nodeIcon = ImageUtilities.loadImageIcon("DesktopSearch/node.svg", false);
        private final ImageIcon edgeIcon = ImageUtilities.loadImageIcon("DesktopSearch/edge.svg", false);

        private final Color selectionBackground = UIManager.getColor("List.selectionBackground");
        private final Color selectionForeground = UIManager.getColor("List.selectionForeground");

        @Override
        public Component getListCellRendererComponent(JList<? extends SearchResult> list, SearchResult value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                isSelected, isSelected);

            // So it looks like if the list had focus
            if (isSelected) {
                renderer.setBackground(selectionBackground);
                renderer.setForeground(selectionForeground);
            }

            Object val = value.getResult();
            if (val instanceof Node) {
                renderer.setIcon(nodeIcon);
            } else if (val instanceof Edge) {
                renderer.setIcon(edgeIcon);
            }

            return renderer;
        }
    }

    private static class ResultsListModel extends AbstractListModel<SearchResult> {

        private final List<SearchResult> results;

        public ResultsListModel(List<SearchResult> results) {
            this.results = results;
        }

        @Override
        public int getSize() {
            return results.size();
        }

        @Override
        public SearchResult getElementAt(int index) {
            return results.get(index);
        }
    }

    @FunctionalInterface
    public interface SimpleDocumentListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }

    /**
     * Dialog that can be dragged by clicking anywhere on it.
     */
    private class DragListener extends MouseInputAdapter {
        Point location;
        MouseEvent pressed;
        Component componentToMove;

        public DragListener(JDialog dialog) {
            componentToMove = dialog;
        }

        public void mousePressed(MouseEvent me) {
            pressed = me;
        }

        public void mouseDragged(MouseEvent me) {
            location = componentToMove.getLocation(location);
            int x = location.x - pressed.getX() + me.getX();
            int y = location.y - pressed.getY() + me.getY();
            componentToMove.setLocation(x, y);
        }
    }

    public static boolean isGraphOpened() {
        return TopComponent.getRegistry().getOpened().stream()
            .map(tc -> WindowManager.getDefault().findTopComponentID(tc))
            .anyMatch(id -> id.equals("GraphTopComponent"));
    }

    public static boolean isDataLabOpened() {
        return TopComponent.getRegistry().getOpened().stream()
            .map(tc -> WindowManager.getDefault().findTopComponentID(tc))
            .anyMatch(id -> id.equals("DataTableTopComponent"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        optionToolbar = new javax.swing.JToolBar();
        searchField = new javax.swing.JTextField();
        categoryToolbar = new javax.swing.JToolBar();
        allCategoriesButton = new javax.swing.JToggleButton();
        resultsList = new javax.swing.JList<>();

        setLayout(new java.awt.BorderLayout());

        topPanel.setLayout(new java.awt.BorderLayout());

        optionToolbar.setRollover(true);
        topPanel.add(optionToolbar, java.awt.BorderLayout.EAST);
        topPanel.add(searchField, java.awt.BorderLayout.SOUTH);

        categoryToolbar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(allCategoriesButton,
            org.openide.util.NbBundle.getMessage(SearchDialog.class,
                "SearchDialog.allCategoriesButton.text")); // NOI18N
        allCategoriesButton.setFocusable(false);
        allCategoriesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        allCategoriesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        categoryToolbar.add(allCategoriesButton);

        topPanel.add(categoryToolbar, java.awt.BorderLayout.CENTER);

        add(topPanel, java.awt.BorderLayout.NORTH);

        resultsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(resultsList, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton allCategoriesButton;
    private javax.swing.JToolBar categoryToolbar;
    private javax.swing.JToolBar optionToolbar;
    private javax.swing.JList<SearchResult> resultsList;
    private javax.swing.JTextField searchField;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
