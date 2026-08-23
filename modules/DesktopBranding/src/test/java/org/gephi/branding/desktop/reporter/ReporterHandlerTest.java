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

package org.gephi.branding.desktop.reporter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Test;

public class ReporterHandlerTest {

    /**
     * The level Exceptions.printStackTrace() logs at (Exceptions.OwnLevel.UNKNOWN),
     * which sits just above SEVERE.
     */
    private static final Level UNKNOWN = new Level("SEVERE", Level.SEVERE.intValue() + 1) {
    };

    private static LogRecord logRecord(String loggerName, Level level) {
        LogRecord record = new LogRecord(level, null);
        record.setLoggerName(loggerName);
        record.setThrown(new IOException("boom"));
        return record;
    }

    @Test
    public void testHandledPlatformDiagnosticsAreNotReported() {
        //Every (logger, level) pair below is a NetBeans Platform catch block that
        //logs and then recovers, so none of them is a crash
        Assert.assertTrue(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler", Level.INFO)));
        Assert.assertTrue(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.core.network.proxy.ProxyAutoConfig", Level.WARNING)));
        Assert.assertTrue(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.core.windows.persistence.WindowManagerParser", Level.INFO)));
        Assert.assertTrue(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.core.windows.persistence", Level.INFO)));
        Assert.assertTrue(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.Stamps", Level.INFO)));
    }

    @Test
    public void testPlatformErrorsAreStillReported() {
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.core.windows.persistence.WindowManagerParser", Level.SEVERE)));
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.netbeans.Stamps", UNKNOWN)));
    }

    @Test
    public void testGephiErrorsAreStillReported() {
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.gephi.desktop.project.ProjectControllerUIImpl", Level.WARNING)));
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.gephi.desktop.project.ProjectControllerUIImpl", Level.SEVERE)));
        //Gephi logs a lot through the root logger, whose name is the empty string
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(logRecord("", Level.WARNING)));
        //Exceptions.printStackTrace(), the usual route for uncaught Gephi errors
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(
            logRecord("org.openide.util.Exceptions", UNKNOWN)));
        Assert.assertFalse(ReporterHandler.isHandledPlatformDiagnostic(logRecord(null, Level.WARNING)));
    }
}
