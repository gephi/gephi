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

package org.gephi.desktop.project;

import java.io.File;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Pins {@link ProjectControllerUIImpl#closeCurrentProject(ProjectControllerUIImpl.CloseDecision)},
 * which is where the confirm/execute split lets the close decision be driven directly, without going
 * through the Event Dispatch Thread dialog or the Project IO executor.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerUIImplTest {

    @Mock
    private ProjectController controller;
    @Mock
    private ImportControllerUI importControllerUI;
    @Mock
    private Project project;

    private ProjectControllerUIImpl impl;

    @Before
    public void setUp() {
        impl = new ProjectControllerUIImpl(controller, importControllerUI);
    }

    @Test
    public void cancelledDecisionDoesNotCloseOrSave() {
        Assert.assertFalse(impl.closeCurrentProject(ProjectControllerUIImpl.CloseDecision.CANCEL));

        Mockito.verify(controller, Mockito.never()).getCurrentProject();
        Mockito.verify(controller, Mockito.never()).saveProject(Mockito.any(), Mockito.any());
        Mockito.verify(controller, Mockito.never()).closeCurrentProject();
    }

    @Test
    public void noCurrentProjectClosesTrivially() {
        Mockito.when(controller.getCurrentProject()).thenReturn(null);

        Assert.assertTrue(impl.closeCurrentProject(ProjectControllerUIImpl.CloseDecision.close(project)));

        Mockito.verify(controller, Mockito.never()).closeCurrentProject();
    }

    @Test
    public void staleDecisionDoesNotActOnADifferentCurrentProject() {
        Project newCurrentProject = Mockito.mock(Project.class);
        Mockito.when(controller.getCurrentProject()).thenReturn(newCurrentProject);

        //The decision was made about "project", but the current project changed to
        //"newCurrentProject" while the user was being asked
        Assert.assertFalse(impl.closeCurrentProject(ProjectControllerUIImpl.CloseDecision.close(project)));

        Mockito.verify(controller, Mockito.never()).saveProject(Mockito.any(), Mockito.any());
        Mockito.verify(controller, Mockito.never()).closeCurrentProject();
    }

    @Test
    public void saveCancelledFromTheProgressBarDoesNotClose() {
        File file = new File("test.gephi");
        Mockito.when(controller.getCurrentProject()).thenReturn(project);
        //controller.saveProject(...) is left unstubbed: it writes nothing and calls back
        //neither saved() nor error(), exactly like a save cancelled from the progress bar

        Assert.assertFalse(
            impl.closeCurrentProject(ProjectControllerUIImpl.CloseDecision.saveAndClose(project, file)));

        Mockito.verify(controller).saveProject(project, file);
        Mockito.verify(controller, Mockito.never()).closeCurrentProject();
    }

    @Test
    public void closeWithoutSavingClosesDirectly() {
        Mockito.when(controller.getCurrentProject()).thenReturn(project);

        Assert.assertTrue(impl.closeCurrentProject(ProjectControllerUIImpl.CloseDecision.close(project)));

        Mockito.verify(controller, Mockito.never()).saveProject(Mockito.any(), Mockito.any());
        Mockito.verify(controller).closeCurrentProject();
    }
}
