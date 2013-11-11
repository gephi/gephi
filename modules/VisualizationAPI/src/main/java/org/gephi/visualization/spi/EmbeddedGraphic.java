package org.gephi.visualization.spi;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;


public interface EmbeddedGraphic {
    void reinit();
    void beforeDisplay(GL gl,GLU glu);
    void display(GL gl,GLU glu);
}

