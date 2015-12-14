#  Copyright 2008-2015 Gephi
#  Website: https://gephi.github.io/
#
# This file is part of Gephi.
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 2011 Gephi Consortium. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 3 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# https://gephi.github.io/developers/license/
# or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License files at
# /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 3, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 3] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 3 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 3 code and therefore, elected the GPL
# Version 3 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
# 
# Contributor(s):
# 
# Portions Copyrighted 2011 Gephi Consortium.

import os, os.path, sys

#Use this script when pulling translations of a new language or new files of existing languages.
#After it, just use tx pull

#Simple script to ensure that .properties files exist for a given language in every folder that a Bundle.properties file exists.
#Creates empty .properties files when not existing. This is necessary to get new language translations that are
#in transifex but not in the repository (tx pull --all is not suitable because it pulls even not translated at all resources).

if (len(sys.argv) < 2):
    print "Usage:"
    print ">>python ./add_language.py {lang}"
    sys.exit(1)

#Creates .properties files of the given language when necessary
def recurseDirs(dir,langBundleName):
    dir = dir.replace('\\', '/')
    containsBundle=False
    containsLangFile=False
    for name in os.listdir(dir):
        fullpath = os.path.join(dir,name)
        if os.path.isfile(fullpath):
            dir, filename = os.path.split(fullpath)
            if filename == "Bundle.properties":
                containsBundle=True
            if filename == langBundleName:
                containsLangFile=True
        elif os.path.isdir(fullpath) and dir.find("target") == -1 and dir.find("modules/branding") == -1 and dir.find("src/java") == -1: #Only search files in code, not build. Also ignore branding module and anything in src/java
            recurseDirs(fullpath,langBundleName)
    
    if containsBundle and not containsLangFile:
        newFilePath=os.path.join(dir,langBundleName)
        print "Adding ",newFilePath
        file = open(newFilePath,"w") #Create empty lang file if not existing and Bundle exists
        file.write("")
        file.close()

recurseDirs("../modules", "Bundle_" + sys.argv[1] + ".properties")
