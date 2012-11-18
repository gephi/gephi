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
package org.gephi.filters.spi;

import javax.swing.Icon;
import org.gephi.filters.api.FilterLibrary;

/**
 * A filter category is like a folder, it describes the type of the filter and
 * bring together to users filters that have the same categories.
 * <p>
 * <b>Default categories are defined in the filter library:</b>
 * <ul><li><code>FilterLibrary.TOPOLOGY</code></li>
 * <li><code>FilterLibrary.ATTRIBUTES</code></li>
 * <li><code>FilterLibrary.EDGE</code></li>
 * <li><code>FilterLibrary.HIERARCHY</code></li></ul>
 * @author Mathieu Bastian
 * @see FilterLibrary
 */
public final class Category {

    private String name;
    private Icon icon;
    private Category parent;

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    public Category(String name, Icon icon, Category parent) {
        this.name = name;
        this.icon = icon;
        this.parent = parent;
    }

    /**
     * Returns the category's name.
     * @return          the name of this category
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the icon or <code>null</code> if the category has no icon.
     * @return          the icon or <code>null</code>
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Returns this category parent category or <code>null</code> if this
     * category has no parent.
     * @return          this category's parent or <code>null</code>
     */
    public Category getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Category) {
            Category cat = (Category) obj;
            if (cat.icon == icon && (cat.name == name || cat.name.equals(name)) && (cat.parent == parent || cat.parent.equals(parent))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.icon != null ? this.icon.hashCode() : 0);
        hash = 29 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        return hash;
    }
}
