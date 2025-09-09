package org.gephi.transformation.api;


import org.gephi.graph.api.Graph;
import org.gephi.transformation.spi.TransformationOperation;

import java.util.Optional;

public interface TransformationController {

    void apply(TransformationOperation operation, Graph graph);
    void mirrorX();

    void mirrorY();

    void rotateLeft1Deg();

    void rotateRight1Deg();

    void rotateLeft45Deg();

    void rotateRight45Deg();

    void extend();

    void reduce();
}
