/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.datalaboratory.api.DataLaboratoryHelper;
import org.gephi.datalaboratory.spi.Manipulator;
import org.gephi.datalaboratory.spi.attributevalues.AttributeValueManipulator;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Utils for building popup menus at right click on nodes/edges rows.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class PopupMenuUtils {

    public static JMenuItem createMenuItemFromManipulator(final Manipulator nm) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(nm.getName());
        if (nm.getDescription() != null && !nm.getDescription().isEmpty()) {
            menuItem.setToolTipText(nm.getDescription());
        }
        menuItem.setIcon(nm.getIcon());
        if (nm.canExecute()) {
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    DataLaboratoryHelper dlh = Lookup.getDefault().lookup(DataLaboratoryHelper.class);
                    dlh.executeManipulator(nm);
                }
            });
        } else {
            menuItem.setEnabled(false);
        }
        return menuItem;
    }

    public static JMenu createSubMenuFromRowColumn(AttributeRow row, AttributeColumn column) {
        DataLaboratoryHelper dlh = Lookup.getDefault().lookup(DataLaboratoryHelper.class);
        JMenu subMenu = new JMenu(NbBundle.getMessage(PopupMenuUtils.class, "Cell.Popup.subMenu.text"));
        subMenu.setIcon(ImageUtilities.loadImageIcon("org/gephi/ui/datatable/resources/table-select.png", true));

        Integer lastManipulatorType = null;
        for (AttributeValueManipulator am : dlh.getAttributeValueManipulators()) {
            am.setup(row, column);
            if (lastManipulatorType == null) {
                lastManipulatorType = am.getType();
            }
            if (lastManipulatorType != am.getType()) {
                subMenu.addSeparator();
            }
            lastManipulatorType = am.getType();
            subMenu.add(PopupMenuUtils.createMenuItemFromManipulator(am));
        }
        if(subMenu.getMenuComponentCount()==0){
            subMenu.setEnabled(false);
        }
        return subMenu;
    }
}
