#  Copyright 2008-2012 Gephi
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

import os.path
import os
import re

project = "gephi"

#Note: gehpi-maven version of this script, use with transifex tool 0.8 or better

#This script sets the initial state of transifex for existing .pot files
#See http://wiki.gephi.org/index.php/Localization for more information
#!!Transifex client must be in the system path to run this script
#If you add 1 or a few pot files, it is faster to do set it manually using a command like the following:
#tx set --auto-local -r gephi.org-gephi-data-attributes-api --source-language=en --source-file org-gephi-data-attributes-api.pot "<lang>.po" --execute#
#This means:
#tx set --auto-local -r project.resource --source-language=en --source-file resource.pot "automatically find translations for this resource in this folder with this expression" --execute

#Searchs for .pot files in subdirectories of the repository and sets them as resources of transifex, also sets its .po translations
#Assumes an executable called transifex in the repository
#This script should be run from gephi repository root
#The result transifex config file exists in .tx/config
#!!Resources with names longer than 50 chars are shortened so they can be correctly pushed
#!!After this script, you should run tx push -s to push new .pot files and optionally -l to push also existing translations

#To update .po translations from Transifex website you have to execute tx pull

directories = [".."]
while len(directories) > 0:
    directory = directories.pop()
    for name in os.listdir(directory):
        fullpath = os.path.join(directory,name)
        if os.path.isfile(fullpath):
            dir, filename = os.path.split(fullpath)
            resource, extension = os.path.splitext(filename)
            if extension == ".pot":
                resourceLen = len(resource)
                if resourceLen > 50: #Maximum of 50 chars for a resource slug, shorten it:
                    print "\n!!Necessary to shorten the following resource (longer than 50 chars): ", resource
                    start = "s-"
                    resource = start + resource[(resourceLen-50+len(start)):resourceLen]
                print "\n", resource
                #set transifex resource
                command="tx set --auto-local -r "+project+"."+resource+" --source-language=en --source-file "+fullpath+" \""+dir+"/<lang>.po\" -t PO --execute"
                os.system(command)
        elif os.path.isdir(fullpath) and directory.find("target") == -1: #Only search pot files in code, not build:
            directories.append(fullpath)
			
