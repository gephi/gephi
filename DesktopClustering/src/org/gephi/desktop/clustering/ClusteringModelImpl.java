/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.desktop.clustering;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.clustering.api.ClusteringModel;
import org.gephi.clustering.spi.Clusterer;

/**
 *
 * @author Mathieu Bastian
 */
public class ClusteringModelImpl implements ClusteringModel {

    //Listeners
    private List<ChangeListener> listeners;
    //Architecture
    private Clusterer selectedClusterer;
    private List<Clusterer> clusterers;
    private boolean running = false;

    public ClusteringModelImpl() {
        clusterers = new ArrayList<Clusterer>();
        listeners = new ArrayList<ChangeListener>();
    }

    public Clusterer getSelectedClusterer() {
        return selectedClusterer;
    }

    public Clusterer[] getClusterers() {
        return clusterers.toArray(new Clusterer[0]);
    }

    public void addClusterer(Clusterer clusterer) {
        for (int i = 0; i < clusterers.size(); i++) {
            if (clusterers.get(i).getClass().equals(clusterer.getClass())) {
                clusterers.set(i, clusterer);
                return;
            }
        }
        clusterers.add(clusterer);
    }

    public void removeClusterer(Clusterer clusterer) {
        clusterers.remove(clusterer);
    }

    public void setSelectedClusterer(Clusterer selectedClusterer) {
        this.selectedClusterer = selectedClusterer;
        fireChangeEvent();
    }

    public void setRunning(boolean running) {
        this.running = running;
        fireChangeEvent();
    }

    public boolean isRunning() {
        return running;
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener changeListener : listeners) {
            changeListener.stateChanged(changeEvent);
        }
    }
}
