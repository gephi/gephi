package org.gephi.preview.supervisor;

import java.awt.Font;
import org.gephi.preview.color.colormode.EdgeBothBColorMode;
import org.gephi.preview.color.colormode.ParentColorMode;

/**
 * Bidirectional edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class BidirectionalEdgeSupervisorImpl extends EdgeSupervisorImpl {

    /**
     * Constructor.
     *
     * Initializes default values.
     */
    public BidirectionalEdgeSupervisorImpl() {
        curvedFlag = false;
        colorizer = new EdgeBothBColorMode();
        showLabelsFlag = true;
        labelMaxChar = 10;
        labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        labelColorizer = new ParentColorMode();
        showMiniLabelsFlag = true;
        miniLabelMaxChar = 10;
        miniLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
        miniLabelAddedRadius = 15f;
        miniLabelColorizer = new ParentColorMode();
        showArrowsFlag = true;
        arrowAddedRadius = 65f;
        arrowSize = 20f;
        arrowColorizer = new ParentColorMode();
    }
}
