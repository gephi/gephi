package org.gephi.viz.engine.pipeline.common;

/**
 *
 * @author Eduardo Ramos
 */
public class InstanceCounter {

    public int unselectedCount = 0;
    public int selectedCount = 0;
    public int unselectedCountToDraw = 0;
    public int selectedCountToDraw = 0;

    public void promoteCountToDraw() {
        unselectedCountToDraw = unselectedCount;
        selectedCountToDraw = selectedCount;
    }

    public void clearCount() {
        unselectedCount = 0;
        selectedCount = 0;
    }

    public int total() {
        return unselectedCount + selectedCount;
    }
    
    public int totalToDraw() {
        return unselectedCountToDraw + selectedCountToDraw;
    }

    @Override
    public String toString() {
        return "InstanceCounter{" + "unselectedCount=" + unselectedCount + ", selectedCount=" + selectedCount + ", unselectedCountToDraw=" + unselectedCountToDraw + ", selectedCountToDraw=" + selectedCountToDraw + '}';
    }
}
