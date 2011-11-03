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
package org.mycompany.installer.wizard.components.sequences;

import org.netbeans.installer.wizard.components.sequences.ProductWizardSequence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mycompany.installer.wizard.components.panels.PostInstallSummaryPanel;
import org.mycompany.installer.wizard.components.panels.PreInstallSummaryPanel;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.panels.LicensesPanel;

/**
 *
 * @author Dmitry Lipin
 */
public class MainSequence extends WizardSequence {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    private DownloadConfigurationLogicAction downloadConfigurationLogicAction;
    private LicensesPanel licensesPanel;
    private PreInstallSummaryPanel preInstallSummaryPanel;
    private UninstallAction uninstallAction;
    private DownloadInstallationDataAction downloadInstallationDataAction;
    private InstallAction installAction;
    private PostInstallSummaryPanel postInstallSummaryPanel;
    private Map<Product, ProductWizardSequence> productSequences;

    public MainSequence() {
        downloadConfigurationLogicAction = new DownloadConfigurationLogicAction();
        licensesPanel = new LicensesPanel();
        preInstallSummaryPanel = new PreInstallSummaryPanel();
        uninstallAction = new UninstallAction();
        downloadInstallationDataAction = new DownloadInstallationDataAction();
        installAction = new InstallAction();
        postInstallSummaryPanel = new PostInstallSummaryPanel();
        productSequences = new HashMap<Product, ProductWizardSequence>();

        installAction.setProperty(InstallAction.TITLE_PROPERTY,
                DEFAULT_IA_TITLE);
        installAction.setProperty(InstallAction.DESCRIPTION_PROPERTY,
                DEFAULT_IA_DESCRIPTION);
    }
    
    @Override
    public void executeForward() {
        final Registry registry = Registry.getInstance();
        final List<Product> toInstall = registry.getProductsToInstall();
        final List<Product> toUninstall = registry.getProductsToUninstall();

        // remove all current children (if there are any), as the components
        // selection has probably changed and we need to rebuild from scratch
        getChildren().clear();

        // the set of wizard components differs greatly depending on the execution
        // mode - if we're installing, we ask for input, run a wizard sequence for
        // each selected component and then download and install; if we're creating
        // a bundle, we only need to download and package things

        if (toInstall.size() > 0) {
            addChild(downloadConfigurationLogicAction);
            addChild(licensesPanel);

            for (Product product : toInstall) {
                if (!productSequences.containsKey(product)) {
                    productSequences.put(
                            product,
                            new ProductWizardSequence(product));
                }

                addChild(productSequences.get(product));
            }
        }

        addChild(preInstallSummaryPanel);

        if (toUninstall.size() > 0) {
            addChild(uninstallAction);
        }

        if (toInstall.size() > 0) {
            addChild(downloadInstallationDataAction);
            addChild(installAction);            
        }

        addChild(postInstallSummaryPanel);
        
        super.executeForward();
    }

    @Override
    public boolean canExecuteForward() {
        return ExecutionMode.NORMAL == ExecutionMode.getCurrentExecutionMode() &&
                (Registry.getInstance().getProductsToInstall().size() > 0 ||
                Registry.getInstance().getProductsToUninstall().size() > 0);
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_IA_TITLE =
            ResourceUtils.getString(
            MainSequence.class,
            "MS.IA.title"); // NOI18N
    public static final String DEFAULT_IA_DESCRIPTION =
            ResourceUtils.getString(
            MainSequence.class,
            "MS.IA.description"); // NOI18N   
    
}
