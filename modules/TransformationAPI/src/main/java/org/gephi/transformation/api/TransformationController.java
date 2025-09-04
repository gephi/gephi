package org.gephi.transformation.api;


import org.gephi.graph.api.Graph;

public interface TransformationController {

    void mirror_x();
    void mirror_y();

    void rotate_left();
    void rotate_right();

    void extend();
    void reduce();
}
