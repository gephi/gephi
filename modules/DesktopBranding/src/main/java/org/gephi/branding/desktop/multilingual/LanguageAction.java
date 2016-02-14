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
package org.gephi.branding.desktop.multilingual;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;

public final class LanguageAction extends CallableSystemAction {

    private static final String APPNAME = "gephi";

    public enum Language {

        EN_US("en", "English"),
        CS_CS("cs", "Čeština"),
        DE_DE("de", "Deutsch"),
        ES_ES("es", "Español"),
        FR_FR("fr", "Français"),
        PT_BR("pt", "BR", "Português do Brasil"),
        RU_RU("ru", "Русский"),
        ZH_CN("zh", "CN", "中文"),
        JA_JA("ja", "日本語");
        private String language;
        private String country = null;
        private String name;

        private Language(String locale, String name) {
            this.language = locale;
            this.name = name;
        }

        private Language(String language, String country, String name) {
            this.language = language;
            this.name = name;
            this.country = country;
        }

        public String getName() {
            return name;
        }

        public String getLanguage() {
            return language;
        }

        public String getCountry() {
            return country;
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

                @Override
                public void actionPerformed(ActionEvent e) {
                    String msg = NbBundle.getMessage(LanguageAction.class, "ChangeLang.Confirm.message");
                    String title = NbBundle.getMessage(LanguageAction.class, "ChangeLang.Confirm.title");
                    DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(msg, title, DialogDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.YES_OPTION)) {
                        try {
                            setLanguage(lang);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, NbBundle.getMessage(LanguageAction.class, "ChangeLang.Error.message"), NbBundle.getMessage(LanguageAction.class, "ChangeLang.Confirm.title"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            //Flag icons from http://www.famfamfam.com
            String iconFile = "org/gephi/branding/desktop/multilingual/resources/" + lang.getLanguage();
            if (lang.getCountry() != null) {
                iconFile += "_" + lang.getCountry();
            }
            iconFile += ".png";
            Icon icon = ImageUtilities.loadImageIcon(iconFile, false);
            if (icon != null) {
                menuItem.setIcon(icon);
            }
            menu.add(menuItem);
        }

        return menu;
    }

    private void setLanguage(Language language) throws Exception {
        String homePath;
        if (Utilities.isMac() || Utilities.isUnix()) {
            homePath = System.getProperty("netbeans.home");
        } else {
            homePath = System.getProperty("user.dir");
        }

        File etc = new File(homePath, "etc");
        if (!etc.exists()) {
            File base = new File(homePath).getParentFile();
            etc = new File(base, "etc");
        }

        File confFile = new File(etc, APPNAME + ".conf");
        StringBuilder outputBuilder = new StringBuilder();

        String matchOptionsLine = "default_options=";

        //In
        BufferedReader reader = new BufferedReader(new FileReader(confFile));
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            if (strLine.contains(matchOptionsLine)) {
                //Remove old language and country:
                strLine = strLine.replaceAll(" -J-Duser\\.language=..", "");
                strLine = strLine.replaceAll(" -J-Duser\\.country=..", "");

                //Get string up to closing '"':
                strLine = strLine.substring(0, strLine.lastIndexOf("\""));
                //Set new language
                strLine += " -J-Duser.language=" + language.getLanguage();
                //Set new country if necessary
                if (language.getCountry() != null) {
                    strLine += " -J-Duser.country=" + language.getCountry();
                }
                //Close options with '"'
                strLine += "\"";
            }

            outputBuilder.append(strLine);
            outputBuilder.append("\n");
        }
        reader.close();

        //Out
        FileWriter writer = new FileWriter(confFile);
        writer.write(outputBuilder.toString());
        writer.close();
        
        LifecycleManager.getDefault().exit();
    }
}
