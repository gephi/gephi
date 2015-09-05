/*
 Copyright 2008-2012 Gephi
 Authors : Eduardo Ramos
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

 Portions Copyrighted 2012 Gephi Consortium.
 */
package org.gephi.preview.api;

import java.awt.event.KeyEvent;

/**
 * <p>Mouse event for preview. Contains the event type and graph coordinates for the event.
 * If you attend a <code>PreviewMouseEvent</code>, it should be marked as consumed.</p>
 * <p>The public keyEvent field contains the keyboard state for the given mouse event. Can be null.</p>
 * @author Eduardo Ramos
 */
public class PreviewMouseEvent {

    public enum Type {
        CLICKED,
        PRESSED,
        RELEASED,
        DRAGGED
    }
    
    public enum Button{
        LEFT,
        RIGHT,
        MIDDLE
    }
    
    public final Type type;
    public final Button button;
    public final int x;
    public final int y;
    private boolean consumed;
    
    /**
     * Contains the keyboard state for the given mouse event. Can be null.
     */
    public final KeyEvent keyEvent;

    public PreviewMouseEvent(int x, int y, Type type, Button button, KeyEvent keyEvent) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.button = button;
        this.keyEvent = keyEvent;
        consumed = false;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}
