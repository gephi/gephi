#  Copyright 2008-2011 Gephi
#  Website : http://www.gephi.org
#
# This file is part of Gephi.
#
# Gephi is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Gephi is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with Gephi.  If not, see <http://www.gnu.org/licenses/>.

import os.path
import os
import re

project = "gephi"

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

print "!Important: please make sure to execute a project clean from Netbeans before executing this script to avoid .pot files conflicts"
#First we make sure that build dir is deleted to avoid setting .pot files from there
if os.path.exists("build"):
	print "Please remove build dir before executing this script to avoid .pot files conflicts"
	exit()
	
directories = ["."]
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
				command="tx set --auto-local -r "+project+"."+resource+" --source-language=en --source-file "+fullpath+" \""+dir+"/<lang>.po\" --execute"
				os.system(command)
        elif os.path.isdir(fullpath):
            directories.append(fullpath)
			