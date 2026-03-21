/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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

package org.gephi.desktop.datalab;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.datalab.api.datatables.DataTablesEventListener;
import org.gephi.datalab.api.datatables.DataTablesEventListenerBuilder;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Provides default instance of DataTableTopComponent as DataTablesEventListener
 *
 * @author Eduardo
 */
@ServiceProvider(service = DataTablesEventListenerBuilder.class)
public class DefaultDataTablesEventListenerBuilder implements DataTablesEventListenerBuilder {

    // Cache the listener reference - TopComponents are long-lived singletons
    private final AtomicReference<DataTablesEventListener> cachedListener = new AtomicReference<>();

    @Override
    public DataTablesEventListener getDataTablesEventListener() {
        // Return cached value if available
        DataTablesEventListener cached = cachedListener.get();
        if (cached != null) {
            return cached;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            DataTableTopComponent component = (DataTableTopComponent) WindowManager.getDefault()
                .findTopComponent("DataTableTopComponent");
            cachedListener.compareAndSet(null, component);
            return component;
        } else {
            // Schedule lookup on EDT and return null for now
            // The caller should retry or handle null gracefully
            SwingUtilities.invokeLater(() -> {
                DataTableTopComponent component = (DataTableTopComponent) WindowManager.getDefault()
                    .findTopComponent("DataTableTopComponent");
                cachedListener.compareAndSet(null, component);
            });

            // Try one more time with invokeAndWait since this is a preparation method
            // that expects a result. Use a short timeout pattern.
            try {
                SwingUtilities.invokeAndWait(() -> {
                    DataTableTopComponent component = (DataTableTopComponent) WindowManager.getDefault()
                        .findTopComponent("DataTableTopComponent");
                    cachedListener.set(component);
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Logger.getLogger(DefaultDataTablesEventListenerBuilder.class.getName())
                    .log(Level.WARNING, "Interrupted while getting DataTableTopComponent", ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(DefaultDataTablesEventListenerBuilder.class.getName())
                    .log(Level.SEVERE, "Error getting DataTableTopComponent", ex);
            }

            return cachedListener.get();
        }
    }
}
