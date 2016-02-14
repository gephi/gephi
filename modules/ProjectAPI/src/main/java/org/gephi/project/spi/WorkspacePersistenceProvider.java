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
package org.gephi.project.spi;

import org.gephi.project.api.Workspace;

/**
 * Interface modules implement to notify the system they can read/write part of
 * the .gephi project file to serialize states and data.
 * <h3>How saving a project works</h3>
 * <ol><li>The saving task is looking for all implementations of this interface
 * and ask each of them to write data either in XML or binary. Each
 * implementation is identified by its identifier, which is provided through
 * <code>getIdentifier()</code>.
 * <li>All of these elements are written in the .gephi project file.
 * </ol>
 * <h3>How loading a project works</h3>
 * <ol><li>The loading task is looking for all implementations of this interface
 * and asks for the identifier returned by <code>getIdentifier()</code>.
 * <li>When traversing the gephi project document it call the provider read
 * method.
 * </ol>
 * <p>
 * Thus this interface allows any module to serialize and deserialize its data
 * to gephi project files.
 * <p>
 * In order to have your <code>WorkspacePersistenceProvider</code> called, you
 * must annotate it with
 * <pre>@ServiceProvider(service = WorkspacePersistenceProvider.class, position = xy)</pre>
 * The <code>position</code> parameter is optional but often useful when when
 * you need other <code>WorkspacePersistenceProvider</code> data deserialized
 * before yours.
 *
 * @author Mathieu Bastian
 * @see Workspace
 */
public interface WorkspacePersistenceProvider {

    /**
     * Unique identifier for your <code>WorkspacePersistenceProvider</code>.
     *
     * @return Unique identifier describing your data
     */
    public String getIdentifier();
}
