/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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

package gephi.visualization.config;

import java.awt.Color;

/**
 *
 * @author Mathieu
 */
public class VizConfig
{
    private int antialiasing=4;
    private boolean lineSmooth=true;
    private boolean lineSmoothNicest=true;
    private boolean pointSmooth=true;
    private boolean blending=true;
    private boolean lighting=true;
    private Color backgroundColor=Color.WHITE;

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isLighting() {
        return lighting;
    }

    public boolean isBlending() {
        return blending;
    }

     public boolean isLineSmoothNicest() {
        return lineSmoothNicest;
    }

    public boolean isLineSmooth() {
        return lineSmooth;
    }

    public boolean isPointSmooth() {
        return pointSmooth;
    }

    public int getAntialiasing() {
        return antialiasing;
    }
}
