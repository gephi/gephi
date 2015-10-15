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
package org.gephi.io.processor.spi;

import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.Importer;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Interface that define the way data are <b>unloaded</b> from containers and
 * appended to the workspace.
 * <p>
 * The purpose of processors is to unload data from the import containers and
 * push it to the workspace, with various strategies. For instance, a processor
 * could either create a new workspace or append data to the current workspace,
 * managing duplicates.
 *
 * @author Mathieu Bastian
 * @see ImportController
 */
public interface Processor {

    /**
     * Process data <b>from</b> the container <b>to</b> the workspace. This task
     * is done after an importer pushed data to the container.
     *
     * @see Importer
     */
    public void process();

    /**
     * Sets the data containers. The processor's job is to get data from the
     * containers and append it to the workspace.
     *
     * @param containers the containers where data are
     */
    public void setContainers(ContainerUnloader[] containers);

    /**
     * Sets the destination workspace for the data in the containers. If no
     * workspace is provided, the current workspace will be used.
     *
     * @param workspace the workspace where data are to be pushed
     */
    public void setWorkspace(Workspace workspace);

    /**
     * Returns the processor's name.
     *
     * @return the processor display name
     */
    public String getDisplayName();

    /**
     * Sets the progress ticket.
     *
     * @param progressTicket progress ticket
     */
    public void setProgressTicket(ProgressTicket progressTicket);
}
