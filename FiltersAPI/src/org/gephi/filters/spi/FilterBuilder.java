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
}
