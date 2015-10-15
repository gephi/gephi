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
package org.gephi.io.importer.impl;

import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.NodeDraft;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDraftImpl extends ElementDraftImpl implements NodeDraft {

    //Flag
    protected boolean createdAuto = false;
    //Viz attributes
    protected float x;
    protected float y;
    protected float z;
    protected float size;
    protected boolean fixed;

    public NodeDraftImpl(ImportContainerImpl container, String id) {
        super(container, id);
    }

    //SETTERS
    public void setCreatedAuto(boolean createdAuto) {
        this.createdAuto = createdAuto;
    }

    @Override
    public void setSize(float size) {
        this.size = size;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    //GETTERS
    @Override
    public float getSize() {
        return size;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getZ() {
        return z;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    public boolean isCreatedAuto() {
        return createdAuto;
    }

    @Override
    ColumnDraft getColumn(String key, Class type) {
        return container.addNodeColumn(key, type);
    }

    @Override
    ColumnDraft getColumn(String key) {
        return container.getNodeColumn(key);
    }

    @Override
    public Iterable<ColumnDraft> getColumns() {
        return container.getNodeColumns();
    }
}
