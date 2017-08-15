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
package org.gephi.branding.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = OptionProcessor.class)
public class CommandLineProcessor extends OptionProcessor {

    private final Option openOption = Option.defaultArguments();
    private final Option openOption2 = Option.additionalArguments('o', "open");
    private final String MEMORY_ERROR;
    private static final String GEPHI_EXTENSION = "gephi";

    public CommandLineProcessor() {
        MEMORY_ERROR = NbBundle.getMessage(CommandLineProcessor.class, "CommandLineProcessor.OutOfMemoryError.message");
    }

    @Override
    protected Set<Option> getOptions() {
        HashSet<Option> set = new HashSet<>();
        set.add(openOption);
        set.add(openOption2);
        return set;
    }

    @Override
    public void process(Env env, Map values) throws CommandException {
        List<String> filenameList = new ArrayList<>();
        Object obj = values.get(openOption);
        if (obj != null) {
            filenameList.addAll(Arrays.asList((String[]) obj));
        }
        obj = values.get(openOption2);
        if (obj != null) {
            filenameList.addAll(Arrays.asList((String[]) obj));
        }
        try {
            for (int i = 0; i < filenameList.size(); i++) {
                File file = new File(filenameList.get(i));
                if (!file.isAbsolute()) {
                    file = new File(env.getCurrentDirectory(), filenameList.get(i));
                }
                FileObject fileObject = FileUtil.toFileObject(file);
                if (!file.exists()) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(CommandLineProcessor.class, "CommandLineProcessor.fileNotFound", file.getName()), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }
                if (fileObject.hasExt(GEPHI_EXTENSION)) {
                    ProjectControllerUI pc = Lookup.getDefault().lookup(ProjectControllerUI.class);
                    try {
                        pc.openProject(file);
                    } catch (Exception ew) {
                        Exceptions.printStackTrace(ew);
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(CommandLineProcessor.class, "CommandLineProcessor.openGephiError"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                    return;
                } else {
                    ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                    if (importController.getImportController().isFileSupported(FileUtil.toFile(fileObject))) {
                        importController.importFile(fileObject);
                    } else {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(CommandLineProcessor.class, "CommandLineProcessor.fileNotSupported"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                }
            }
        } catch (OutOfMemoryError ex) {
            System.gc();
            NotifyDescriptor nd = new NotifyDescriptor.Message(MEMORY_ERROR, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            NotifyDescriptor nd = new NotifyDescriptor.Message("CommandLineParsing " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
