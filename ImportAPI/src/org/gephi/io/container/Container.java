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
package org.gephi.io.container;

/**
 *
 * @author Mathieu Bastian
 */
public interface Container {

    public enum ErrorMode {

        NO_ERROR, REPORT, ALL
    };

    public void setErrorMode(ErrorMode errorMode);

    public ErrorMode getErrorMode();

    public void setSource(String source);

    public String getSource();

    public ContainerLoader getLoader();

    public ContainerUnloader getUnloader();

    public void setAllowSelfLoop(boolean value);

    public void setAllowAutoNode(boolean value);

    public void setAllowParallelEdge(boolean value);

    public ContainerReport getReport();

    public interface ContainerReport {

        public void append(String str);

        public void append(Exception e);

        public String getReport();
    }
}
