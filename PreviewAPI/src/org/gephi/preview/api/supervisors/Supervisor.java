package org.gephi.preview.api.supervisors;

import org.gephi.preview.api.SupervisorPropery;

/**
 * Generic interface of a supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Supervisor {

    /**
     * Clears the list of supervised elements.
     */
    public void clearSupervised();

    /**
     * Return properties for this supervisor. Can get or set values.
     */
    public SupervisorPropery[] getProperties();
}
