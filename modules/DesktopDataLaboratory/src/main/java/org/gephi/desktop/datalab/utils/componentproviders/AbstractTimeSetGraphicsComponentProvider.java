package org.gephi.desktop.datalab.utils.componentproviders;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.utils.TimeIntervalGraphics;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.JRendererLabel;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractTimeSetGraphicsComponentProvider extends ComponentProvider<JLabel> {

    protected static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    protected static final Color UNSELECTED_BACKGROUND = Color.white;
    protected static final Color FILL_COLOR = new Color(153, 255, 255);
    protected static final Color BORDER_COLOR = new Color(2, 104, 255);

    protected final TimeIntervalGraphics timeIntervalGraphics;

    protected final JXTable table;
    protected final GraphModelProvider graphModelProvider;
    protected JRendererLabel rendererLabel;

    public AbstractTimeSetGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(null, JLabel.LEADING);
        this.graphModelProvider = graphModelProvider;
        this.table = table;
        this.timeIntervalGraphics = new TimeIntervalGraphics(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    private String getTextFromValue(Object value) {
        TimeSet timeSet = (TimeSet) value;
        String text = null;
        if (timeSet != null) {
            text = timeSet.toString(graphModelProvider.getGraphModel().getTimeFormat(), graphModelProvider.getGraphModel().getTimeZone());
        }

        return text;
    }

    @Override
    protected void format(CellContext context) {
        //Set image or text
        int witdth = table.getColumnModel().getColumn(context.getColumn()).getWidth();
        int height = table.getRowHeight(context.getRow());

        String text = getTextFromValue(context.getValue());

        rendererLabel.setSize(witdth, height);
        rendererLabel.setToolTipText(text);
        rendererLabel.setBorder(null);

        setImagePainter((TimeSet) context.getValue(), context.isSelected());
    }

    @Override
    protected void configureState(CellContext context) {
    }

    @Override
    protected JLabel createRendererComponent() {
        return rendererLabel = new JRendererLabel();
    }

    protected class TimeIntervalGraphicsParameters {

        private final double[] starts;
        private final double[] ends;

        public TimeIntervalGraphicsParameters(double[] starts, double[] ends) {
            this.starts = starts;
            this.ends = ends;
        }
    }

    public abstract TimeIntervalGraphicsParameters getTimeIntervalGraphicsParameters(TimeSet value);

    public void setImagePainter(TimeSet value, boolean isSelected) {
        if (value == null) {
            rendererLabel.setPainter(null);
            return;
        }

        Color background;
        if (isSelected) {
            background = SELECTED_BACKGROUND;
        } else {
            background = UNSELECTED_BACKGROUND;
        }

        TimeIntervalGraphicsParameters params = getTimeIntervalGraphicsParameters(value);

        final BufferedImage image = timeIntervalGraphics.createTimeIntervalImage(
                params.starts,
                params.ends,
                rendererLabel.getWidth() - 1,
                rendererLabel.getHeight() - 1,
                FILL_COLOR,
                BORDER_COLOR,
                background
        );

        rendererLabel.setPainter(new ImagePainter(image));
    }

    public double getMax() {
        return timeIntervalGraphics.getMax();
    }

    public double getMin() {
        return timeIntervalGraphics.getMin();
    }

    public void setMinMax(double min, double max) {
        timeIntervalGraphics.setMinMax(min, max);
    }
}
