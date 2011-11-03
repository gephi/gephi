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

import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.mycompany.installer.utils.applications.NetBeansRCPUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
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
public class PreInstallSummaryPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    public PreInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        setProperty(INSTALLATION_FOLDER_PROPERTY,
                DEFAULT_INSTALLATION_FOLDER);

        setProperty(UNINSTALL_LABEL_TEXT_PROPERTY,
                DEFAULT_UNINSTALL_LABEL_TEXT);

        setProperty(INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);

        setProperty(NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_NEXT_BUTTON_TEXT);

        setProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY,
                DEFAULT_ERROR_NOT_ENOUGH_SPACE);
        setProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CHECK_SPACE);
        setProperty(ERROR_LOGIC_ACCESS_PROPERTY,
                DEFAULT_ERROR_LOGIC_ACCESS);
        setProperty(ERROR_FSROOTS_PROPERTY,
                DEFAULT_ERROR_FSROOTS);
        setProperty(ERROR_NON_EXISTENT_ROOT_PROPERTY,
                DEFAULT_ERROR_NON_EXISTENT_ROOT);
        setProperty(ERROR_CANNOT_WRITE_PROPERTY,
                DEFAULT_ERROR_CANNOT_WRITE);
        setProperty(REMOVE_APP_USERDIR_TEXT_PROPERTY,
                DEFAULT_REMOVE_APP_USERDIR_TEXT);
        setProperty(REMOVE_APP_USERDIR_CHECKBOX_PROPERTY,
                DEFAULT_REMOVE_APP_USERDIR_CHECKBOX);
        
    }

    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PreInstallSummaryPanelUi(this);
        }

        return wizardUi;
    }

    @Override
    public void initialize() {
        final List<Product> toInstall =
                Registry.getInstance().getProductsToInstall();

        if (toInstall.size() > 0) {
            setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT);
            setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        } else {
            setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT_UNINSTALL);
            setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION_UNINSTALL);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PreInstallSummaryPanelUi extends ErrorMessagePanelUi {

        protected PreInstallSummaryPanel component;

        public PreInstallSummaryPanelUi(PreInstallSummaryPanel component) {
            super(component);

            this.component = component;
        }

        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PreInstallSummaryPanelSwingUi(component, container);
            }

            return super.getSwingUi(container);
        }
    }

    public static class PreInstallSummaryPanelSwingUi extends ErrorMessagePanelSwingUi {

        protected PreInstallSummaryPanel component;
        private NbiTextPane locationsPane;
        private NbiLabel uninstallListLabel;
        private NbiTextPane uninstallListPane;
        private NbiLabel installationSizeLabel;
        private NbiLabel installationSizeValue;
        private NbiLabel downloadSizeLabel;
        private NbiLabel downloadSizeValue;
        private NbiCheckBox removeUserdirCheckbox;
        private NbiTextPane removeUserdirPane;
        private NbiPanel spacer;
        private int gridy = 0;

        public PreInstallSummaryPanelSwingUi(
                final PreInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);

            this.component = component;
            initComponents();
        }

        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();

            container.getNextButton().setText(
                    panel.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
        }

        @Override
        protected void initialize() {
            final Registry registry = Registry.getInstance();

            final StringBuilder text = new StringBuilder();
            long installationSize = 0;
            long downloadSize = 0;

            for (Product product : registry.getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
                downloadSize += product.getDownloadSize();


            }

            // add top-level components like nb-base, glassfish, tomcat, jdk
            for (Product product : registry.getProductsToInstall()) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(panel.getProperty(INSTALLATION_FOLDER_PROPERTY),
                        product.getDisplayName()));
                text.append(StringUtils.LF);
                text.append("    " + product.getInstallationLocation());
                text.append(StringUtils.LF);
            }
            locationsPane.setText(text);

            List<Product> toUninstall = registry.getProductsToUninstall();
            String uninstallLabelText = toUninstall.size() > 0 ? StringUtils.format(
                    panel.getProperty(UNINSTALL_LABEL_TEXT_PROPERTY),
                    toUninstall.get(0).getDisplayName()) : "";

            uninstallListLabel.setText(uninstallLabelText);

            
            installationSizeLabel.setText(
                    panel.getProperty(INSTALLATION_SIZE_PROPERTY));
            installationSizeValue.setText(StringUtils.formatSize(
                    installationSize));

            downloadSizeLabel.setText(
                    panel.getProperty(DOWNLOAD_SIZE_PROPERTY));
            downloadSizeValue.setText(StringUtils.formatSize(
                    downloadSize));

            if (registry.getProductsToInstall().size() == 0) {
                locationsPane.setVisible(false);
                installationSizeLabel.setVisible(false);
                installationSizeValue.setVisible(false);
            } else {
                locationsPane.setVisible(true);
                installationSizeLabel.setVisible(true);
                installationSizeValue.setVisible(true);
            }

            if (registry.getProductsToUninstall().size() == 0) {
                uninstallListLabel.setVisible(false);
                uninstallListPane.setVisible(false);
            } else {
                uninstallListLabel.setVisible(true);
                uninstallListPane.setVisible(true);
            }

            downloadSizeLabel.setVisible(false);
            downloadSizeValue.setVisible(false);
            for (RegistryNode remoteNode : registry.getNodes(RegistryType.REMOTE)) {
                if (remoteNode.isVisible()) {
                    downloadSizeLabel.setVisible(true);
                    downloadSizeValue.setVisible(true);
                }
            }

            if (Boolean.getBoolean(REMOVE_APP_USERDIR_PROPERTY)) {
                removeUserdirCheckbox.doClick();
            }

            removeUserdirCheckbox.setVisible(false);
            removeUserdirPane.setVisible(false);
            
            for (Product product : Registry.getInstance().getProductsToUninstall()) {

                try {
                    File installLocation = product.getInstallationLocation();
                    LogManager.log("... product installation directory: " + installLocation);
                    File userDir = NetBeansRCPUtils.getApplicationUserDirFile(installLocation);
                    LogManager.log("... product userdir: " + userDir);
                    if (FileUtils.exists(userDir) && FileUtils.canWrite(userDir)) {
                        removeUserdirCheckbox.setText(
                                StringUtils.format(
                                panel.getProperty(REMOVE_APP_USERDIR_CHECKBOX_PROPERTY),
                                userDir.getAbsolutePath()));
                        removeUserdirCheckbox.setBorder(new EmptyBorder(0, 0, 0, 0));
                        removeUserdirCheckbox.setVisible(true);

                        removeUserdirPane.setVisible(true);
                        removeUserdirPane.setContentType("text/html");
                        
                        removeUserdirPane.setText(
                                StringUtils.format(
                                panel.getProperty(REMOVE_APP_USERDIR_TEXT_PROPERTY),
                                product.getDisplayName()));

                    }
                    break;
                } catch (IOException e) {
                    LogManager.log(e);
                }

            }

            //if(productCheckboxList!=null) {
            //    for(Pair <Product, NbiCheckBox> pair : productCheckboxList) {
            //        pair.getSecond().doClick();
            //    }
            //}            
            super.initialize();
        }

        @Override
        protected String validateInput() {
            try {
                if (!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {
                    final List<File> roots =
                            SystemUtils.getFileSystemRoots();
                    final List<Product> toInstall =
                            Registry.getInstance().getProductsToInstall();
                    final Map<File, Long> spaceMap =
                            new HashMap<File, Long>();

                    LogManager.log("Available roots : " + StringUtils.asString(roots));

                    File downloadDataDirRoot = FileUtils.getRoot(
                            Installer.getInstance().getLocalDirectory(), roots);
                    long downloadSize = 0;
                    for (Product product : toInstall) {
                        downloadSize += product.getDownloadSize();
                    }
                    // the critical check point - we download all the data
                    spaceMap.put(downloadDataDirRoot, new Long(downloadSize));
                    long lastDataSize = 0;
                    for (Product product : toInstall) {
                        final File installLocation = product.getInstallationLocation();
                        final File root = FileUtils.getRoot(installLocation, roots);
                        final long productSize = product.getRequiredDiskSpace();

                        LogManager.log("    [" + root + "] <- " + installLocation);

                        if (root != null) {
                            Long ddSize = spaceMap.get(downloadDataDirRoot);
                            // remove space that was freed after the remove of previos product data
                            spaceMap.put(downloadDataDirRoot,
                                    Long.valueOf(ddSize - lastDataSize));

                            // add space required for next product installation
                            Long size = spaceMap.get(root);
                            size = Long.valueOf(
                                    (size != null ? size.longValue() : 0L)
                                    + productSize);
                            spaceMap.put(root, size);
                            lastDataSize = product.getDownloadSize();
                        } else {
                            return StringUtils.format(
                                    panel.getProperty(ERROR_NON_EXISTENT_ROOT_PROPERTY),
                                    product, installLocation);
                        }
                    }

                    for (File root : spaceMap.keySet()) {
                        try {
                            final long availableSpace =
                                    SystemUtils.getFreeSpace(root);
                            final long requiredSpace =
                                    spaceMap.get(root) + REQUIRED_SPACE_ADDITION;

                            if (availableSpace < requiredSpace) {
                                return StringUtils.format(
                                        panel.getProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY),
                                        root,
                                        StringUtils.formatSize(requiredSpace - availableSpace));
                            }
                        } catch (NativeException e) {
                            ErrorManager.notifyError(
                                    panel.getProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY),
                                    e);
                        }
                    }
                }

                final List<Product> toUninstall =
                        Registry.getInstance().getProductsToUninstall();
                for (Product product : toUninstall) {
                    if (!FileUtils.canWrite(product.getInstallationLocation())) {
                        return StringUtils.format(
                                panel.getProperty(ERROR_CANNOT_WRITE_PROPERTY),
                                product,
                                product.getInstallationLocation());
                    }
                }

            } catch (IOException e) {
                ErrorManager.notifyError(
                        panel.getProperty(ERROR_FSROOTS_PROPERTY), e);
            }

            return null;
        }

        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            gridy = 0;

            // locationsPane ////////////////////////////////////////////////////////
            locationsPane = new NbiTextPane();

            // uninstallListPane ////////////////////////////////////////////////////
            uninstallListPane = new NbiTextPane();

            // uninstallListLabel ///////////////////////////////////////////////////
            uninstallListLabel = new NbiLabel();
            uninstallListLabel.setLabelFor(uninstallListPane);

            // installationSizeValue ////////////////////////////////////////////////
            installationSizeValue = new NbiLabel();
            //installationSizeValue.setFocusable(true);

            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            installationSizeLabel.setLabelFor(installationSizeValue);

            // downloadSizeValue ////////////////////////////////////////////////////
            downloadSizeValue = new NbiLabel();
            //downloadSizeValue.setFocusable(true);

            // downloadSizeLabel ////////////////////////////////////////////////////
            downloadSizeLabel = new NbiLabel();
            downloadSizeLabel.setLabelFor(downloadSizeValue);

            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();

            // this /////////////////////////////////////////////////////////////////
            add(locationsPane, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(uninstallListLabel, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(uninstallListPane, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(0, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???            
            int gridy0 = gridy;
            gridy++;

            removeUserdirPane = new NbiTextPane();
            add(removeUserdirPane, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???

            removeUserdirCheckbox = new NbiCheckBox();
            add(removeUserdirCheckbox, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 20, 0, 11), // padding
                    0, 0));                           // padx, pady - ???

            removeUserdirCheckbox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.setProperty(REMOVE_APP_USERDIR_PROPERTY,
                            "" + removeUserdirCheckbox.isSelected());
                }
            });

            add(installationSizeLabel, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(22, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(installationSizeValue, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 22, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeLabel, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeValue, new GridBagConstraints(
                    0, gridy++, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(4, 22, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, gridy + 10, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
        }
    }
/////////////////////////////////////////////////////////////////////////////////
// Constants
    public static final String INSTALLATION_FOLDER_PROPERTY =
            "installation.folder"; // NOI18N
    public static final String UNINSTALL_LABEL_TEXT_PROPERTY =
            "uninstall.list.label.text"; // NOI18N
    public static final String INSTALLATION_SIZE_PROPERTY =
            "installation.size"; // NOI18N
    public static final String DOWNLOAD_SIZE_PROPERTY =
            "download.size"; // NOI18N
    public static final String ERROR_NOT_ENOUGH_SPACE_PROPERTY =
            "error.not.enough.space"; // NOI18N
    public static final String ERROR_CANNOT_CHECK_SPACE_PROPERTY =
            "error.cannot.check.space"; // NOI18N
    public static final String ERROR_LOGIC_ACCESS_PROPERTY =
            "error.logic.access"; // NOI18N
    public static final String ERROR_FSROOTS_PROPERTY =
            "error.fsroots"; // NOI18N
    public static final String ERROR_NON_EXISTENT_ROOT_PROPERTY =
            "error.non.existent.root"; // NOI18N
    public static final String ERROR_CANNOT_WRITE_PROPERTY =
            "error.cannot.write"; // NOI18N
    public static final String REMOVE_APP_USERDIR_PROPERTY =
            "remove.app.userdir";
    public static final String REMOVE_APP_USERDIR_TEXT_PROPERTY =
            "remove.app.userdir.text";
    public static final String REMOVE_APP_USERDIR_CHECKBOX_PROPERTY =
            "remove.app.userdir.checkbox";
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.description"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_UNINSTALL =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.description.uninstall"); // NOI18N
    public static final String DEFAULT_INSTALLATION_FOLDER =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.installation.folder"); // NOI18N
    public static final String DEFAULT_UNINSTALL_LABEL_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.uninstall.list.label.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.download.size"); // NOI18N
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.next.button.text"); // NOI18N
    public static final String DEFAULT_NEXT_BUTTON_TEXT_UNINSTALL =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.next.button.text.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_ENOUGH_SPACE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.not.enough.space"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.cannot.check.space");// NOI18N
    public static final String DEFAULT_ERROR_LOGIC_ACCESS =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.logic.access");// NOI18N
    public static final String DEFAULT_ERROR_FSROOTS =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.fsroots"); // NOI18N
    public static final String DEFAULT_ERROR_NON_EXISTENT_ROOT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.non.existent.root"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_WRITE =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.error.cannot.write"); // NOI18N
    public static final String DEFAULT_REMOVE_APP_USERDIR_TEXT =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.remove.app.userdir.text"); // NOI18N
    
    public static final String DEFAULT_REMOVE_APP_USERDIR_CHECKBOX =
            ResourceUtils.getString(PreInstallSummaryPanel.class,
            "PrISP.remove.app.userdir.checkbox"); // NOI18N
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
