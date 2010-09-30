/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.branding.desktop.multilingual;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;

public final class LanguageAction extends CallableSystemAction {

    private static final String APPNAME = "gephidesktop";

    public enum Language {

        EN_US("en", "English"),
        FR_FR("fr", "Français"),
        ES_ES("es", "Español");
        private String locale;
        private String name;

        private Language(String locale, String name) {
            this.locale = locale;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getLocale() {
            return locale;
        }
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(LanguageAction.class, "CTL_LanguageAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new JMenu(NbBundle.getMessage(LanguageAction.class, "CTL_LanguageAction"));
        for (final Language lang : Language.values()) {
            JMenuItem menuItem = new JMenuItem(new AbstractAction(lang.getName()) {

                public void actionPerformed(ActionEvent e) {
                    String msg = NbBundle.getMessage(LanguageAction.class, "ChangeLang.Confirm.message" + (Utilities.isMac() ? ".mac" : ""));
                    String title = NbBundle.getMessage(LanguageAction.class, "ChangeLang.Confirm.title");
                    DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(msg, title, DialogDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.YES_OPTION)) {
                        setLanguage(lang);
                    }
                }
            });
            //Flag icons from http://www.famfamfam.com
            Icon icon = ImageUtilities.loadImageIcon("org/gephi/branding/desktop/multilingual/resources/" + lang.getLocale() + ".png", false);
            if (icon != null) {
                menuItem.setIcon(icon);
            }
            menu.add(menuItem);
        }

        return menu;
    }

    private void setLanguage(Language language) {
        String homePath;
        if (Utilities.isMac()) {
            homePath = System.getProperty("netbeans.home");
        } else {
            homePath = System.getProperty("user.dir");
        }

        File etc = new File(homePath, "etc");
        if (!etc.exists()) {
            File base = new File(homePath).getParentFile();
            etc = new File(base, "etc");
        }

        File confFile = new File(etc, APPNAME+".conf");
        try {
            StringBuilder outputBuilder = new StringBuilder();
            String match = "-J-Duser.language=";
            int langLength = 2;

            //In
            BufferedReader reader = new BufferedReader(new FileReader(confFile));
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                int i = 0;
                if ((i = strLine.indexOf(match)) != -1) {
                    String locale = strLine.substring(i + match.length());
                    locale = locale.substring(0, langLength);
                    String before = strLine.substring(0, i + match.length());
                    String after = strLine.substring(i + match.length() + langLength);
                    outputBuilder.append(before);
                    outputBuilder.append(language.getLocale());
                    outputBuilder.append(after);
                } else {
                    outputBuilder.append(strLine);
                }
                outputBuilder.append("\n");
            }
            reader.close();

            //Out
            FileWriter writer = new FileWriter(confFile);
            writer.write(outputBuilder.toString());
            writer.close();

            //Restart
            if (!Utilities.isMac()) {
                //On Mac the change is applied only if restarted manually
                LifecycleManager.getDefault().markForRestart();
            }
            LifecycleManager.getDefault().exit();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
}
