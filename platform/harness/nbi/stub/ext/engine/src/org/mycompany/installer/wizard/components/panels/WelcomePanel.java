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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Dmitry Lipin
 */
public class WelcomePanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    private Registry bundledRegistry;
    private Registry defaultRegistry;

    public WelcomePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        setProperty(WELCOME_TEXT_PROPERTY,
                DEFAULT_WELCOME_TEXT);

        setProperty(WELCOME_ALREADY_INSTALLED_TEXT_PROPERTY,
                DEFAULT_WELCOME_ALREADY_INSTALLED_TEXT);
        setProperty(WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT);

       try {
            defaultRegistry = Registry.getInstance();
            bundledRegistry = new Registry();

            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);
            if (bundledRegistryUri != null) {
                bundledRegistry.loadProductRegistry(bundledRegistryUri);
            } else {
                bundledRegistry.loadProductRegistry(
                        Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
            }
        } catch (InitializationException e) {
            ErrorManager.notifyError("Cannot load bundled registry", e);
        }

    }

    Registry getBundledRegistry() {
        return bundledRegistry;
    }

    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WelcomePanelUi(this);
        }

        return wizardUi;
    }

    @Override
    public boolean canExecuteForward() {
        return canExecute();
    }

    @Override
    public boolean canExecuteBackward() {
        return canExecute();
    }

        // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return bundledRegistry.getNodes().size() > 1;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WelcomePanelUi extends ErrorMessagePanelUi {

        protected WelcomePanel component;

        public WelcomePanelUi(WelcomePanel component) {
            super(component);

            this.component = component;
        }

        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WelcomePanelSwingUi(component, container);
            }

            return super.getSwingUi(container);
        }
    }

    public static class WelcomePanelSwingUi extends ErrorMessagePanelSwingUi {

        protected WelcomePanel panel;
        private NbiTextPane textPane;

        private NbiPanel leftImagePanel;
        ValidatingThread validatingThread;

        public WelcomePanelSwingUi(
                final WelcomePanel component,
                final SwingContainer container) {
            super(component, container);

            this.panel = component;

            initComponents();
        }

        @Override
        public String getTitle() {
            return null; // the welcome page does not have a title
        }

        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();

            container.getBackButton().setVisible(false);
        }

        @Override
        protected void initialize() {

            textPane.setContentType("text/html");
            textPane.setText(StringUtils.format(panel.getProperty(WELCOME_TEXT_PROPERTY)));
            List<Product> toInstall = Registry.getInstance().getProductsToInstall();
            if(toInstall.isEmpty()) {
                List <Product> list = panel.getBundledRegistry().getProducts();
                if(list.size() == 1) {
                    if(SystemUtils.getCurrentPlatform().isCompatibleWith(list.get(0).getPlatforms())) {
                        File installationLocation = list.get(0).getInstallationLocation();
                        textPane.setText(
                            StringUtils.format(
                            panel.getProperty(WELCOME_ALREADY_INSTALLED_TEXT_PROPERTY),
                            list.get(0).getDisplayName(),
                            installationLocation.getAbsolutePath()));
                    } else {
                        textPane.setText(
                            StringUtils.format(
                            WELCOME_INCOMPATIBLE_PLATFORM_TEXT,
                            list.get(0).getDisplayName()));
                    }
                    container.getCancelButton().setVisible(false);
                    container.getNextButton().setText(panel.getProperty(
                            WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT_PROPERTY));
                }
            }            

            super.initialize();
        }

        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();

            leftImagePanel = new NbiPanel();
            int width = 0;
            int height = 0;
            final String topLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY));
            final String bottomLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY));

            int bottomAnchor = NbiPanel.ANCHOR_BOTTOM_LEFT;

            if (topLeftImage != null) {
                leftImagePanel.setBackgroundImage(topLeftImage, ANCHOR_TOP_LEFT);
                width = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconWidth();
                height += leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconHeight();
            }
            if (bottomLeftImage != null) {
                leftImagePanel.setBackgroundImage(bottomLeftImage, bottomAnchor);
                width = leftImagePanel.getBackgroundImage(bottomAnchor).getIconWidth();
                height += leftImagePanel.getBackgroundImage(bottomAnchor).getIconHeight();
            }

            leftImagePanel.setPreferredSize(new Dimension(width, height));
            leftImagePanel.setMaximumSize(new Dimension(width, height));
            leftImagePanel.setMinimumSize(new Dimension(width, 0));
            leftImagePanel.setSize(new Dimension(width, height));

            leftImagePanel.setOpaque(false);
            // this /////////////////////////////////////////////////////////////////
            int dy = 0;
            add(leftImagePanel, new GridBagConstraints(
                    0, 0, // x, y
                    1, 100, // width, height
                    0.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.WEST, // anchor
                    GridBagConstraints.VERTICAL, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???
            add(textPane, new GridBagConstraints(
                    1, dy++, // x, y
                    4, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(10, 11, 11, 11), // padding
                    0, 0));                           // padx, pady - ???
            
            NbiTextPane separatorPane = new NbiTextPane();

            separatorPane = new NbiTextPane();
            add(separatorPane, new GridBagConstraints(
                    3, dy, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???


            // move error label after the left welcome image
            Component errorLabel = getComponent(0);
            getLayout().removeLayoutComponent(errorLabel);
            add(errorLabel, new GridBagConstraints(
                    1, 99, // x, y
                    99, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 11, 4, 0), // padding
                    0, 0));                            // ??? (padx, pady)


        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.title");
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.description"); // NOI18N
    public static final String WELCOME_TEXT_PROPERTY =
            "welcome.text"; // NOI18N
    public static final String WELCOME_ALREADY_INSTALLED_TEXT_PROPERTY =
            "welcome.already.installed.text"; // NOI18N
    public static final String WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT_PROPERTY =
            "welcome.already.installed.next.button.text";//NOI18N
    public static final String WELCOME_INCOMPATIBLE_PLATFORM_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.incompatible.platform.text");//NOI18N

    public static final String DEFAULT_WELCOME_ALREADY_INSTALLED_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.already.installed.next.button.text");//NOI18N
    
    public static final String DEFAULT_WELCOME_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.welcome.text"); // NOI18N

    public static final String DEFAULT_WELCOME_ALREADY_INSTALLED_TEXT =
            ResourceUtils.getString(WelcomePanel.class,
            "WP.already.installed.text"); // NOI18N

    public static final String WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.top.image";//NOI18N
    public static final String WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.bottom.image";//NOI18N
}
