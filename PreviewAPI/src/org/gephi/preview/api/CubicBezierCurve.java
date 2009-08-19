package org.gephi.preview.api;

import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface CubicBezierCurve {

    public PVector getPt1();

    public PVector getPt2();
    
    public PVector getPt3();

    public PVector getPt4();

}
