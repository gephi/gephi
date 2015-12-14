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

import os.path
import os
import re

project = "gephi"

#Note: gephi-maven version of this script, use with transifex tool 0.11 or better

#This script sets the initial state of transifex for existing .properties files
#See https://github.com/gephi/gephi/wiki/Localization for more information
#!!Transifex client must be in the system path to run this script
#If you add 1 or a few .properties files, it is faster to do set it manually using a command like the following:
#tx set --auto-local -r gephi.org-gephi-data-attributes-api --source-language=en --source-file Bundle.properties "Bundle_<lang>.properties" --execute#
#This means:
#tx set --auto-local -r project.resource --source-language=en --source-file Bundle.properties "automatically find translations for this resource in this folder with this expression" --execute

#Searchs for .properties files in subdirectories of the repository and sets them as resources of transifex, also sets its .properties translations
#Assumes an executable called transifex in the repository
#This script should be run from gephi/translations repository folder
#The result transifex config file exists in .tx/config
#!!Resources with names longer than 50 chars are shortened so they can be correctly pushed
#!!After this script, you should run tx push -s to push new .properties files and optionally -t to push also existing translations. Use command "tx help push" for more info

#To update .properties translations from Transifex website you have to execute tx pull

def get_resource_name(directory):
    part = None
    parts = []
    directory, part = os.path.split(directory)
    while directory != '' and (part != 'resources' or not directory.endswith('main')):
        parts.append(part)
        directory, part = os.path.split(directory)
    parts.reverse()
    return '-'.join(parts)


directories = [".."]
while len(directories) > 0:
    directory = directories.pop()
    directory = directory.replace('\\', '/')
    for name in os.listdir(directory):
        fullpath = os.path.join(directory, name)
        if os.path.isfile(fullpath):
            dir, filename = os.path.split(fullpath)
            if filename == "Bundle.properties":
                resource = get_resource_name(dir)
                resourceLen = len(resource)
                if resourceLen > 50: #Maximum of 50 chars for a resource slug, shorten it:
                    print "\n!!Necessary to shorten the following resource (longer than 50 chars): ", resource
                    start = "s-"
                    resource = start + resource[(resourceLen-50+len(start)):resourceLen]
                print "\n", resource
                #set transifex resource
                command="tx set --auto-local -r "+project+"."+resource+" --source-language=en --source-file "+fullpath+" \""+dir+"/Bundle_<lang>.properties\" -t PROPERTIES --execute"
                os.system(command)
        elif os.path.isdir(fullpath) and directory.find("target") == -1 and directory.find("modules/branding") == -1 and directory.find("src/java") == -1: #Only search files in code, not build. Also ignore branding module and anything in src/java
            directories.append(fullpath)