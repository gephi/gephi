
package org.gephi.preview.api;

import org.gephi.preview.spi.Renderer;

/**
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class ManagedRenderer {
    private Renderer renderer;
    private boolean enabled;

    public ManagedRenderer(Renderer renderer, boolean enabled) {
        this.renderer = renderer;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
}
