/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.workspace.spi;

import org.gephi.workspace.api.Workspace;

/**
 *
 * @author Mathieu Bastian
 */
public interface WorkspaceDuplicateProvider {

    public void duplicate(Workspace source, Workspace destination);
}
