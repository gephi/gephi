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
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterPanelPanel extends JPanel implements ChangeListener {

    private Query selectedQuery;
    private final String settingsString;
    private FilterUIModel uiModel;

    public FilterPanelPanel() {
        super(new BorderLayout());
        settingsString = NbBundle.getMessage(FilterPanelPanel.class, "FilterPanelPanel.settings");
        if (UIUtils.isAquaLookAndFeel()) {
            setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshModel();
    }

    private void refreshModel() {
        if (uiModel != null) {
            if (uiModel.getSelectedQuery() != selectedQuery) {
                selectedQuery = uiModel.getSelectedQuery();
                setQuery(selectedQuery);
            }
        } else {
            setQuery(null);
        }
    }

    public void setup(FilterUIModel model) {
        uiModel = model;
        if (model != null) {
            model.addChangeListener(this);
        }
        refreshModel();
    }

    public void unsetup() {
        if (uiModel != null) {
            uiModel.removeChangeListener(this);
            uiModel = null;
            refreshModel();
        }
    }

    private void setQuery(final Query query) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //UI update
                removeAll();
                setBorder(null);
                if (query != null) {
                    FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                    FilterBuilder builder = filterController.getModel().getLibrary().getBuilder(query.getFilter());
                    try {
                        JPanel panel = builder.getPanel(query.getFilter());
                        if (panel != null) {
                            add(panel, BorderLayout.CENTER);
                            panel.setOpaque(false);
                            setBorder(javax.swing.BorderFactory.createTitledBorder(query.getFilter().getName() + " " + settingsString));
                        }
                    } catch (Exception e) {
                        Logger.getLogger("").log(Level.SEVERE, "Error while setting query", e);
                    }
                }

                revalidate();
                repaint();
            }
        });

    }
}
