/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

/**
 *
 * @author Mathieu Bastian
 */
public interface GraphExporter extends Exporter {

    /**
     * Sets if only the visible graph has to be exported. If <code>false</code>,
     * the complete graph is exported.
     * @param exportVisible the export visible parameter value
     */
    public void setExportVisible(boolean exportVisible);

    /**
     * Returns <code>true</code> if only the visible graph has to be exported.
     * @return  <code>true</code> if only the visible graph has to be exported,
     *          <code>false</code> for the complete graph.
     */
    public boolean isExportVisible();
}
