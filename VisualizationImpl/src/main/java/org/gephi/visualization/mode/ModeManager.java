/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
