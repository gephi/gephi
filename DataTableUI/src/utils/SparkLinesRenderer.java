/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package utils;

import com.representqueens.spark.LineGraph;
import com.representqueens.spark.SizeParams;
import java.awt.Component;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.NumberList;

/**
 * TableCellRenderer for drawing sparklines from cells that have a NumberList or DynamicNumber as their value.
 */
public class SparkLinesRenderer extends DefaultTableCellRenderer {

    private static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    private static final Color UNSELECTED_BACKGROUND = Color.white;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            //Render empty string when null
            return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }

        Number[] numbers=null;
        if (value instanceof NumberList) {
            numbers = getNumberListNumbers((NumberList) value);
        } else if(value instanceof DynamicType){
            numbers=getDynamicNumberNumbers((DynamicType) value);
        }else{
            throw new IllegalArgumentException("Only number lists and dynamic numbers are supported for sparklines rendering");
        }

        //If there is less than 2 elements, show as a String.
        if (numbers.length < 2) {
            return super.getTableCellRendererComponent(table, value.toString(), isSelected, hasFocus, row, column);
        }

        JLabel label = new JLabel();

        Color background;
        if (isSelected) {
            background = SELECTED_BACKGROUND;
        } else {
            background = UNSELECTED_BACKGROUND;
        }

        final SizeParams size = new SizeParams(table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight(row) - 1, 1);
        final BufferedImage i = LineGraph.createGraph(numbers, size, Color.BLUE, background);
        label.setIcon(new ImageIcon(i));
        label.setToolTipText(value.toString());//String representation as tooltip

        return label;
    }

    private Number[] getNumberListNumbers(NumberList numberList) {
        ArrayList<Number> numbers = new ArrayList<Number>();
        Number n;
        for (int i = 0; i < numberList.size(); i++) {
            n = (Number) numberList.getItem(i);
            if (n != null) {
                numbers.add(n);
            }
        }
        return numbers.toArray(new Number[0]);
    }

    private Number[] getDynamicNumberNumbers(DynamicType dynamicNumber){
        ArrayList<Number> numbers = new ArrayList<Number>();
        if (dynamicNumber == null) {
            return new Number[0];
        }
        Number[] dynamicNumbers;
        dynamicNumbers = (Number[]) dynamicNumber.getValues().toArray(new Number[0]);
        Number n;
        for (int i = 0; i < dynamicNumbers.length; i++) {
            n = (Number) dynamicNumbers[i];
            if (n != null) {
                numbers.add((Number) n);
            }
        }
        return numbers.toArray(new Number[0]);
    }
}
