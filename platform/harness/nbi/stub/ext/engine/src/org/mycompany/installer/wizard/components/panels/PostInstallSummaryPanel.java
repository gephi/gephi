/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.mycompany.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.containers.SwingFrameContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_INSTALL;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_UNINSTALL;

/**
 *
 * @author Dmitry Lipin
 */
public class PostInstallSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    public PostInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        setProperty(MESSAGE_TEXT_SUCCESS_PROPERTY,
                DEFAULT_MESSAGE_TEXT_SUCCESS);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS);
        setProperty(MESSAGE_TEXT_WARNINGS_PROPERTY,
                DEFAULT_MESSAGE_TEXT_WARNINGS);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS);
        setProperty(MESSAGE_TEXT_ERRORS_PROPERTY,
                DEFAULT_MESSAGE_TEXT_ERRORS);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS);

        setProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL);


        setProperty(NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_NEXT_BUTTON_TEXT);
    }

    @Override
    public boolean isPointOfNoReturn() {
        return true;
    }

    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PostInstallSummaryPanelUi(this);
        }

        return wizardUi;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PostInstallSummaryPanelUi extends WizardPanelUi {

        protected PostInstallSummaryPanel component;

        public PostInstallSummaryPanelUi(PostInstallSummaryPanel component) {
            super(component);

            this.component = component;
        }

        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PostInstallSummaryPanelSwingUi(component, container);
            }

            return super.getSwingUi(container);
        }
    }

    public static class PostInstallSummaryPanelSwingUi extends WizardPanelSwingUi {

        protected PostInstallSummaryPanel component;
        private NbiTextPane messagePaneInstall;
        private NbiTextPane messagePaneUninstall;


        private NbiTextPane messagePaneFinish;
        private NbiCheckBox runAppNow;
        private Product app;

        public PostInstallSummaryPanelSwingUi(
                final PostInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);

            this.component = component;

            initComponents();
        }

        protected void initializeContainer() {
            super.initializeContainer();

            // set up the back button
            container.getBackButton().setVisible(false);
            container.getBackButton().setEnabled(false);

            // set up the next (or finish) button
            container.getNextButton().setVisible(true);
            container.getNextButton().setEnabled(true);

            container.getNextButton().setText(
                    component.getProperty(NEXT_BUTTON_TEXT_PROPERTY));

            // set up the cancel button
            container.getCancelButton().setVisible(false);
            container.getCancelButton().setEnabled(false);
        }

        @Override
        public void evaluateNextButtonClick() {
            container.getNextButton().setEnabled(false);
            final Product product = app;
            if (app != null) {
                ProductConfigurationLogic l = null;
                try {
                    l = app.getLogic();
                } catch (InitializationException e) {
                }
                final File executable = l != null ? new File(app.getInstallationLocation(), l.getExecutable()) : null;

                if (executable != null && runAppNow.isSelected()) {
                    LogManager.log("... running: " + executable.getAbsolutePath());
                    ProcessBuilder pb = new ProcessBuilder(new String[]{executable.getAbsolutePath()});
                    try {
                        pb.start();
                    } catch (IOException e) {
                        LogManager.log(e);
                    }

                }
            }
            super.evaluateNextButtonClick();
        }

        protected void initialize() {
            final Registry registry = Registry.getInstance();
            final List<Product> successfulInstall = registry.getProducts(INSTALLED_SUCCESSFULLY);
            final List<Product> warningInstall = registry.getProducts(INSTALLED_WITH_WARNINGS);
            final List<Product> errorInstall = registry.getProducts(FAILED_TO_INSTALL);

            final List<Product> successfulUninstall = registry.getProducts(UNINSTALLED_SUCCESSFULLY);
            final List<Product> warningUninstall = registry.getProducts(UNINSTALLED_WITH_WARNINGS);
            final List<Product> errorUninstall = registry.getProducts(FAILED_TO_UNINSTALL);

            if (errorInstall.size() > 0) {
                messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY));
                messagePaneInstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_ERRORS_PROPERTY),
                        errorInstall.get(0).getDisplayName(),
                        LogManager.getLogFile()));
            } else if (warningInstall.size() > 0) {
                messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY));
                messagePaneInstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_WARNINGS_PROPERTY),
                        warningInstall.get(0).getDisplayName(),
                        LogManager.getLogFile()));
            } else if (successfulInstall.size() > 0) {
                messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY));
                messagePaneInstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_SUCCESS_PROPERTY),
                        successfulInstall.get(0).getDisplayName(),
                        LogManager.getLogFile()));
            } else {
                messagePaneInstall.setVisible(false);
            }

            messagePaneFinish.setVisible(true);
            messagePaneFinish.setContentType(DEFAULT_MESSAGE_FINISH_PROCESS_CONTENT_TYPE);
            messagePaneFinish.setText(DEFAULT_MESSAGE_FINISH_PROCESS);

            if (errorUninstall.size() > 0) {
                messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY));
                messagePaneUninstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY),
                        errorUninstall.get(0).getDisplayName(),
                        LogManager.getLogFile()));
            } else if (warningUninstall.size() > 0) {
                messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY));
                messagePaneUninstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY),
                        warningUninstall.get(0).getDisplayName(),
                        LogManager.getLogFile()));
            } else if (successfulUninstall.size() > 0) {
                messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY));
                messagePaneUninstall.setText(StringUtils.format(
                        component.getProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY),
                        successfulUninstall.get(0).getDisplayName(),
                        LogManager.getLogFile()));
            } else {
                messagePaneUninstall.setVisible(false);
            }

    

            final List<Product> products = new LinkedList<Product>();
            products.addAll(successfulInstall);
            products.addAll(warningInstall);
            runAppNow.setSelected(false);
            if (!products.isEmpty()) {
                runAppNow.setText(StringUtils.format(DEFAULT_MESSAGE_LAUNCH_APPLICATION_NOW,
                        products.get(0).getDisplayName()));
                runAppNow.doClick();
                app = products.get(0);
            } else {                
                runAppNow.setVisible(false);
            }


        }

        private void initComponents() {
            // messagePaneInstall ///////////////////////////////////////////////////
            messagePaneInstall = new NbiTextPane();

            // messagePaneUninstall /////////////////////////////////////////////////
            messagePaneUninstall = new NbiTextPane();

            // messagePaneRunApplication ///////////////////////////////////////////////////
            

            messagePaneFinish = new NbiTextPane();

            runAppNow = new NbiCheckBox();


            // this /////////////////////////////////////////////////////////////////
            add(messagePaneInstall, new GridBagConstraints(
                    0, 0, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(31, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneUninstall, new GridBagConstraints(
                    0, 1, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(31, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???

            add(messagePaneFinish, new GridBagConstraints(
                    0, 3, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            /*
            add(new NbiPanel(), new GridBagConstraints(
                    0, 4, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            */
            add(runAppNow, new GridBagConstraints(
                    0, 4, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???



            if (container instanceof SwingFrameContainer) {
                final SwingFrameContainer sfc = (SwingFrameContainer) container;
                sfc.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent event) {
                        SwingUi currentUi = component.getWizardUi().getSwingUi(container);
                        if (currentUi != null) {
                            if (!container.getCancelButton().isEnabled() && // cancel button is disabled
                                    !container.getCancelButton().isVisible() && // no cancel button at this panel
                                    !container.getBackButton().isVisible() && // no back button at this panel
                                    container.getNextButton().isVisible() && // next button is visible
                                    container.getNextButton().isEnabled()) { // and enabled                                                                
                                currentUi.evaluateNextButtonClick();
                                sfc.removeWindowListener(this);
                            }
                        }
                    }
                });
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_TEXT_SUCCESS_PROPERTY =
            "message.text.success"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY =
            "message.content.type.success"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_PROPERTY =
            "message.text.warnings"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY =
            "message.content.type.warnings"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_PROPERTY =
            "message.text.errors"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY =
            "message.content.type.errors"; // NOI18N
    public static final String MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY =
            "message.text.success.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY =
            "message.content.type.success.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY =
            "message.text.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY =
            "message.content.type.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY =
            "message.text.errors.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY =
            "message.content.type.errors.uninstall"; // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.content.type.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.content.type.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.content.type.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.content.type.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.content.type.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.errors.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.content.type.errors.uninstall"); // NOI18N
    
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            PostInstallSummaryPanel.class,
            "PoISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.description"); // NOI18N
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.next.button.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_FINISH_PROCESS =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.finish"); // NOI18N
    public static final String DEFAULT_MESSAGE_FINISH_PROCESS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.text.finish.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_LAUNCH_APPLICATION_NOW =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.run.application.now");//NOI18N
}
