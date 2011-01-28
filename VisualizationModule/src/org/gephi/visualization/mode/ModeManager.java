/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.mode;

import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.DisplayMode;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class ModeManager implements VizArchitecture {

    public enum AVAILABLE_MODES {FULL, VISIBLE,HIGHLIGHT};
    private AbstractEngine engine;
    private DisplayMode[] modes;
    private DisplayMode currentMode;
    private DisplayMode futureMode;
    private boolean requireModeChange = false;

    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();

        //Init modes;
        modes = new DisplayMode[3];
        modes[0] = new FullGraphMode(engine);
        modes[1] = new VisibleGraphMode(engine);
        modes[2] = new HighLightGraphMode(engine);

        currentMode = modes[0];
    }

    public synchronized void selectMode(AVAILABLE_MODES mode) {
        DisplayMode m=null;
        switch(mode) {
            case FULL:
                m = modes[0];
                break;
            case VISIBLE:
                m = modes[1];
                break;
            case HIGHLIGHT:
                m = modes[2];
                break;
        }
        if(m!=currentMode) {
            futureMode = m;
            requireModeChange = true;
        }
    }

    public boolean requireModeChange() {
        return requireModeChange;
    }

    public synchronized void changeMode() {
        currentMode = futureMode;
        futureMode = null;
        requireModeChange = false;
    }

    public void unload() {
        currentMode.unload();
    }

    public AVAILABLE_MODES getMode() {
        if(requireModeChange) {
            return futureMode.getMode();
        }
        return currentMode.getMode();
    }
}
