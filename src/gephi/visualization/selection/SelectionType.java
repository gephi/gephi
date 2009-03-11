/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.selection;

/**
 *
 * @author Mathieu
 */
public interface SelectionType {
    
    public SelectionArea getSelectionArea();
	public void setSelectionArea(SelectionArea selectionArea);
    public String getName();
	public void setName(String name);
}
