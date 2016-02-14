package org.gephi.desktop.datalab.utils.componentproviders;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.utils.sparklines.SparklineGraph;
import org.gephi.utils.sparklines.SparklineParameters;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.JRendererLabel;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractSparklinesGraphicsComponentProvider extends ComponentProvider<JLabel> {

    protected static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    protected static final Color UNSELECTED_BACKGROUND = Color.white;

    protected final GraphModelProvider graphModelProvider;
    protected final JXTable table;
    protected JRendererLabel rendererLabel;

    public AbstractSparklinesGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(null, JLabel.LEADING);
        this.graphModelProvider = graphModelProvider;
        this.table = table;
    }

    public abstract String getTextFromValue(Object value);

    @Override
    protected void format(CellContext context) {
        //Set image or text
        int witdth = table.getColumnModel().getColumn(context.getColumn()).getWidth();
        int height = table.getRowHeight(context.getRow());

        String text = getTextFromValue(context.getValue());

        rendererLabel.setSize(witdth, height);
        rendererLabel.setToolTipText(text);
        rendererLabel.setBorder(null);

        setImagePainter(context.getValue(), context.isSelected());
    }

    @Override
    protected void configureState(CellContext context) {
    }

    @Override
    protected JLabel createRendererComponent() {
        return rendererLabel = new JRendererLabel();
    }

    public void setImagePainter(Object value, boolean isSelected) {
        if (value == null) {
            rendererLabel.setPainter(null);
            return;
        }

        Number[][] values = getSparklinesXAndYNumbers(value);
        Number[] xValues = values[0];
        Number[] yValues = values[1];

        //If there is less than 1 element, don't show anything.
        if (yValues.length < 1) {
            rendererLabel.setPainter(null);
            return;
        }

        if (yValues.length == 1) {
            //SparklineGraph needs at least 2 values, duplicate the only one we have to get a sparkline with a single line showing that the value does not change over time
            xValues = null;
            yValues = new Number[]{yValues[0], yValues[0]};
        }

        Color background;
        if (isSelected) {
            background = SELECTED_BACKGROUND;
        } else {
            background = UNSELECTED_BACKGROUND;
        }

        //Note: Can't use interactive SparklineComponent because TableCellEditors don't receive mouse events.
        final SparklineParameters sparklineParameters = new SparklineParameters(
                rendererLabel.getWidth() - 1,
                rendererLabel.getHeight() - 1,
                Color.BLUE,
                background,
                Color.RED,
                Color.GREEN,
                null
        );
        final BufferedImage image = SparklineGraph.draw(xValues, yValues, sparklineParameters);

        rendererLabel.setPainter(new ImagePainter(image));
    }

    public abstract Number[][] getSparklinesXAndYNumbers(Object value);
}
