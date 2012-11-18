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
package org.gephi.visualization.api.objects;

import java.util.List;
import org.gephi.visualization.api.initializer.Modeler;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class ModelClass {

    private final String name;
    private int classId;
    private boolean enabled;
    private int cacheMarker;
    private int selectionId = 0;

    //Config
    private boolean lod;
    private boolean selectable;
    private boolean clickable;
    private boolean glSelection;
    private boolean aloneSelection;

    public ModelClass(String name, boolean lod, boolean selectable, boolean clickable, boolean glSelection, boolean aloneSelection) {
        this.name = name;
        this.lod = lod;
        this.selectable = selectable;
        this.clickable = clickable;
        this.glSelection = glSelection;
        this.aloneSelection = aloneSelection;
    }

    public ModelClass() {
        this.name = "";
    }

    public abstract void addModeler(Modeler modeler);

    public abstract Modeler getCurrentModeler();

    public abstract void setCurrentModeler(Modeler modeler);

    public abstract void setCurrentModeler(String className);

    public abstract List<? extends Modeler> getModelers();

    public abstract void swapModelers();

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public boolean isLod() {
        return lod;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isClickable() {
        return clickable;
    }

    public boolean isGlSelection() {
        return glSelection;
    }

    public boolean isAloneSelection() {
        return aloneSelection;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCacheMarker() {
        return cacheMarker;
    }

    public void setCacheMarker(int cacheMarker) {
        this.cacheMarker = cacheMarker;
    }

    public int getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(int selectionId) {
        this.selectionId = selectionId;
    }
}
