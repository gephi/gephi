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

/**
 * Category builder is a convenient way to define multiple builders from a single
 * source and grouped in a single category.
 * <p>
 * Implement <code>CategoryBuilder</code>
 * for instance for creating a set of filter builders working on attributes, with
 * one <code>FilterBuilder</code> per attribute column.
 * <p>
 * Note that filter builders returned by category builders don't have to be
 * registered on they own, once here is enough.
 *
 * @author Mathieu Bastian
 * @see FilterBuilder
 */
public interface CategoryBuilder {

    /**
     * Returns the filter builders this category builder is building.
     * @return  the builders this category builder is building
     */
    public FilterBuilder[] getBuilders();

    /**
     * Returns the category builders are to be grouped in. It can't be a
     * default category.
     * @return  the category builders belong to
     */
    public Category getCategory();
}
