/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.mrufiles.api;

import java.util.List;

/**
 *
 * @author Mathieu Bastian
 */
public interface MostRecentFiles {

    public void addFile(String absolutePath);

    public List<String> getMRUFileList();
}
