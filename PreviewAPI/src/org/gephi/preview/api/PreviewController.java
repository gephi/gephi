package org.gephi.preview.api;

/**
 * Interface of the preview controller.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface PreviewController {

    /**
     * Returns the current preview graph sheet.
     * 
     * @return the current preview graph sheet
     */
    public GraphSheet getGraphSheet();

    /**
     * Returns a portion of the current preview graph sheet.
     *
     * @param visibilityRatio  the ratio of the preview graph to display
     * @return                 a portion of the current preview graph sheet
     */
    public GraphSheet getPartialGraphSheet(float visibilityRatio);

    public PreviewModel getModel();

    public PreviewPreset[] getDefaultPresets();

    public PreviewPreset[] getUserPresets();

    public void savePreset(String name);

    public void setCurrentPreset(PreviewPreset preset);

    public void setBackgroundColor(java.awt.Color color);
}
