/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.branding.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.io.importer.api.ImportController;
import org.gephi.project.api.ProjectController;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class CommandLineProcessor extends OptionProcessor {

    private Option openOption = Option.defaultArguments();
    private Option openOption2 = Option.additionalArguments('o', "open");
    private final String MEMORY_ERROR;
    private static final String GEPHI_EXTENSION = "gephi";

    public CommandLineProcessor() {
        MEMORY_ERROR = NbBundle.getMessage(CommandLineProcessor.class, "CommandLineProcessor.OutOfMemoryError.message");
    }

    @Override
    protected Set<Option> getOptions() {
        HashSet<Option> set = new HashSet<Option>();
        set.add(openOption);
        set.add(openOption2);
        return set;
    }

    @Override
    public void process(Env env, Map values) throws CommandException {
        List<String> filenameList = new ArrayList<String>();
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
                if (fileObject.hasExt(GEPHI_EXTENSION)) {
                    ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                    DataObject doe = DataObject.find(fileObject);
                    pc.openProject(doe).run();
                    return;
                } else {
                    ImportController importController = Lookup.getDefault().lookup(ImportController.class);
                    if (importController.isFileSupported(fileObject)) {
                        importController.doImport(fileObject);
                    }
                }
            }
        } catch (OutOfMemoryError ex) {
            System.gc();
            NotifyDescriptor nd = new NotifyDescriptor.Message(MEMORY_ERROR, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (Exception ex) {
            ex.printStackTrace();
            NotifyDescriptor nd = new NotifyDescriptor.Message("CommandLineParsing " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
