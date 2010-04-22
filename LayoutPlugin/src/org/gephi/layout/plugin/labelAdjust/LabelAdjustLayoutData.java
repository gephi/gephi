/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.layout.plugin.labelAdjust;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.ForceVectorNodeLayoutData;

/**
 *
 * @author Mathieu Bastian
 */
public class LabelAdjustLayoutData extends ForceVectorNodeLayoutData {

    public List<Node> neighbours = new ArrayList<Node>();
}
