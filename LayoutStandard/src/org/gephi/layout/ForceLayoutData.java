/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout;

import org.gephi.layout.force.ForceVector;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class ForceLayoutData extends ForceVector {

    public float energy0;
    public float step;
    public int progress;

    public ForceLayoutData() {
        progress = 0;
        step = 0;
        energy0 = Float.POSITIVE_INFINITY;
    }
}
