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
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;

/**
 * Filter builder, creating <code>Filter</code> instances for a <b>single</b> type
 * of filters. Provides also the settings panel for the type of filter.
 * <p>
 * Implementors should add the <code>@ServiceProvider</code> annotation to be
 * registered by the system or call <code>FilterLibrary.addBuilder()</code>.
 * <p>
 * The <code>JPanel</code> returned by the <code>getPanel()</code> method is the
 * settings panel that configures the filter parameters. These parameters can be
 * get and set by using {@link Filter#getProperties()}. Settings panel should
 * always set parameters values in that way. As a result the system will be aware
 * values changed and update the filter.
 * <p>
 * See {@link CategoryBuilder} for builders that host multiple types of filters.
 * @author Mathieu Bastian
 * @see FilterLibrary
 */
public interface FilterBuilder {

    /**
     * Returns the category this filter builder belongs to.
     * @return          the category this builder belongs to
     */
    public Category getCategory();

    /**
     * Returns the display name of this filter builder
     * @return          the display name
     */
    public String getName();

    /**
     * Returns the icon of this filter builder
     * @return          the icon
     */
    public Icon getIcon();

    /**
     * Returns ths description text of this filter builder
     * @return          the description
     */
    public String getDescription();

    /**
     * Builds a new <code>Filter</code> instance.
     * @return          a new <code>Filter</code> object
     */
    public Filter getFilter();

    /**
     * Returns the settings panel for the filter this builder is building, the
     * <code>filter</code> object is passed as a parameter.
     * @param filter    the filter that the panel is to be configuring
     * @return          the filter's settings panel
     */
    public JPanel getPanel(Filter filter);

    /**
     * Notification when the filter is destroyed, to perform clean-up tasks.
     */
    public void destroy(Filter filter);
}
