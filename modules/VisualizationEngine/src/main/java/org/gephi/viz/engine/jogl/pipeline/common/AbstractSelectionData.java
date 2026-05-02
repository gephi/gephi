package org.gephi.viz.engine.jogl.pipeline.common;

abstract public class AbstractSelectionData {
    protected long startedTime = 0L;
    protected boolean selectionToggle = false;
    protected float globalTime = 0f;
    protected float selectedTime = 0f;

    protected volatile boolean someSelection;

    protected void refreshTime() {
        globalTime = (System.currentTimeMillis() - this.startedTime) / 1000.0f;

        if (selectionToggle != someSelection) {
            selectionToggle = someSelection;
            selectedTime = globalTime;
        }
    }

}
