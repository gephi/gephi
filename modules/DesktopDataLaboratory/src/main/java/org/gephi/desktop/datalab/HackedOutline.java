
package org.gephi.desktop.datalab;

import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.swing.outline.*;

class HackedOutline extends Outline {

    protected TableColumn createColumn(int modelIndex) {
	return new HackedOutlineColumn(modelIndex);
    }

    public class HackedOutlineColumn extends OutlineColumn {

	public HackedOutlineColumn(int modelIndex) {
	    super(modelIndex);
	}
       
	// Copied private method from netbeans...ETable.
	private int estimatedWidth(Object dataObject, JTable table) {
	    TableCellRenderer cr = getCellRenderer();
	    if (cr == null) {
		Class c = table.getModel().getColumnClass(modelIndex);
		cr = table.getDefaultRenderer(c);
	    }
	    Component c = cr.getTableCellRendererComponent(table, dataObject, false,
							   false, 0, table.getColumnModel().getColumnIndex(getIdentifier()));
	    return c.getPreferredSize().width;
	}

	// Copy of ETable::updatePreferredWidth that avoids extensive calculation for large tables.
	void updatePreferredWidth(JTable table) {

	    TableModel dataModel = table.getModel();
	    int rows = dataModel.getRowCount();
	    if (rows == 0) {
		return;
	    }
	    int sum = 0;
	    int max = 15;

	    // For long tables, just hope the first block of cells are typical of the table.
	    if(rows > 10000)
		rows = 10000;

	    for (int i = 0; i < rows; i++) {
		Object data = dataModel.getValueAt(i, modelIndex);
		int estimate = estimatedWidth(data, table);
		sum += estimate;
		if (estimate > max) {
		    max = estimate;
		}
	    }
	    max += 5;
	    setPreferredWidth(max);

	}

    }

    // Copy more crap to hack around Java visibility

    public void setModel(TableModel dataModel) {

	// Avoid running updatePreferredWidths from within setModel
	Hashtable backup = defaultRenderersByColumnClass;
	defaultRenderersByColumnClass = null;
	super.setModel(dataModel);
	defaultRenderersByColumnClass = backup;

	// Now use my own hacked update:
	if(backup != null)
	    updatePreferredWidths();

    }

    private void updatePreferredWidths() {
	Enumeration en = getColumnModel().getColumns();
	while (en.hasMoreElements()) {
	    Object obj = en.nextElement();
	    if (obj instanceof HackedOutlineColumn) {
		HackedOutlineColumn hoc = (HackedOutlineColumn) obj;
		hoc.updatePreferredWidth(this);
	    }
	}
    }
    
}
