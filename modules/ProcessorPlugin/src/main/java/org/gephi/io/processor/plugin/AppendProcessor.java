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
package org.gephi.io.processor.plugin;

import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processor 'Append graph' that tries to find in the current workspace nodes and edges in the container to only append new elements. It uses elements' id to do the matching.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class, position = 100)
public class AppendProcessor extends DefaultProcessor implements Processor {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AppendProcessor.class, "AppendProcessor.displayName");
    }

    @Override
    public void process() {
        try {
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            //Workspace
            if (workspace == null) {
                workspace = pc.getCurrentWorkspace();
            }

            if (workspace != null) {
                pc.openWorkspace(workspace);
            } else {
                workspace = pc.newWorkspace(pc.getCurrentProject());
            }
            
            for (ContainerUnloader container : containers) {
                processConfiguration(container, workspace);

                if (container.getSource() != null) {
                    pc.setSource(workspace, container.getSource());
                }

                process(container, workspace);
            }
        } finally {
            clean();
        }
    }
}
