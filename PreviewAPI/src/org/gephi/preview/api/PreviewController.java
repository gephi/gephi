/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
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
