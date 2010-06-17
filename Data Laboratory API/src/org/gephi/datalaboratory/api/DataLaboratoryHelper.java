/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.api;

import org.gephi.datalaboratory.spi.Manipulator;
import org.gephi.datalaboratory.spi.edges.EdgesManipulator;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;

/**
 * Interface for simplifying the implementation of Data Laboratory.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface DataLaboratoryHelper {

    /**
     * Prepares an array with one new instance of every NodesManipulator
     * that has a builder registered and returns it.
     * It also must ensure to return the manipulators ordered first by type and then by position
     * @return Array of all NodesManipulator implementations
     */
    NodesManipulator[] getNodesManipulators();

    /**
     * Prepares an array with one new instance of every EdgesManipulator
     * that has a builder registered and returns it.
     * It also must ensure to return the manipulators ordered first by type and then by position
     * @return Array of all EdgesManipulator implementations
     */
    EdgesManipulator[] getEdgesManipulators();

    /**
     * Prepares the dialog UI of a manipulator if it has one and executes the manipulator in a separate
     * Thread when the dialog is accepted or directly if there is no UI.
     * @param m Manipulator to execute
     */
    void executeManipulator(Manipulator m);

    /**
     * Special method for making public DeleteNodes manipulator so it can be specifically retrieved from Data Table UI.
     * It is used for reacting to delete key.
     * @return DeleteNodes new instance
     */
    NodesManipulator getDeleteNodesManipulator();

    /**
     * Special method for making public DeleteEdges manipulator so it can be specifically retrieved from Data Table UI.
     * It is used for reacting to delete key.
     * @return DeleteEdges new instance
     */
    EdgesManipulator getDeleEdgesManipulator();
}
