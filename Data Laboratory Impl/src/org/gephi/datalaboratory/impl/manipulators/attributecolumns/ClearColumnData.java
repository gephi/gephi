/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.datalaboratory.impl.manipulators.attributecolumns;

import java.awt.Image;
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalaboratory.api.AttributesController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class ClearColumnData implements AttributeColumnsManipulator {

    public void execute(AttributeTable table, AttributeColumn column) {
        if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(ClearColumnData.class, "ClearColumnData.confirmation.message",column.getTitle()), getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Lookup.getDefault().lookup(AttributesController.class).clearColumnData(table, column);
            Lookup.getDefault().lookup(DataTablesController.class).selectTable(table);
        }
    }

    public String getName() {
        return NbBundle.getMessage(ClearColumnData.class, "ClearColumnData.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        if (table == ac.getModel().getNodeTable()) {
            //Can clear columns with DATA origin and label of nodes:
            return column.getOrigin() == AttributeOrigin.DATA || column.getIndex() == PropertiesColumn.NODE_LABEL.getIndex();
        } else {
            //Can clear columns with DATA origin and label of edges:
            return column.getOrigin() == AttributeOrigin.DATA || column.getIndex() == PropertiesColumn.EDGE_LABEL.getIndex();
        }
    }

    public AttributeColumnsManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 100;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/table-delete-column.png");
    }
}
