package org.gephi.desktop.search.popup;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.gephi.desktop.search.api.SearchResult;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

public class ActionPopup extends MouseAdapter {

    private final JList<SearchResult> list;

    public ActionPopup(JList<SearchResult> list) {
        super();
        this.list = list;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybePopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybePopup(e);
    }

    private void maybePopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            SwingUtilities.invokeLater(() -> {
                final Point p = e.getPoint();
                int row = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(row);
                final JPopupMenu pop = createPopup(p);
                if (pop != null) {
                    showPopup(p.x, p.y, pop);
                }
            });
        }
    }

    protected JPopupMenu createPopup(Point p) {
        SearchResult result = list.getSelectedValue();
        if (result != null) {
            if (result.getResult() instanceof Node) {
                return NodePopup.createPopup((Node) result.getResult());
            } else if (result.getResult() instanceof Edge) {
                return EdgePopup.createPopup((Edge) result.getResult());
            }
        }
        return null;
    }

    private void showPopup(int xpos, int ypos, final JPopupMenu popup) {
        if ((popup != null) && (popup.getSubElements().length > 0)) {
            popup.show(list, xpos, ypos);
        }
    }
}
