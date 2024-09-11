package org.gephi.layout.plugin.forceAtlas2.force;

import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;

/**
 *
 * @author totetmatt
 */
public abstract class AFA2Force {
    final protected ForceAtlas2.ForceAtlas2Params params;
    public AFA2Force(org.gephi.layout.plugin.forceAtlas2.ForceAtlas2.ForceAtlas2Params params) {
        this.params = params;
    }

    
}
