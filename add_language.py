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

import os, os.path, sys

#Use this script when pulling translations of a new language or new files of existing languages.
#After it, just use tx pull

#Simple script to ensure that .po files exist for a given language in every folder that a .pot file exists.
#Creates empty .po files when not existing. This is necessary to get new language translations that are
#in transifex but not in the repository (tx pull --all is not suitable because it pulls even not translated at all resources).

if (len(sys.argv) < 2):
    print "Usage:"
    print ">>python ./add_language.py {lang}"
    sys.exit(1)

#Creates po files of the given language when necessary
def recurseDirs(dir,langPO):
	containsPOT=False
	containsLangPO=False
	for name in os.listdir(dir):
		fullpath = os.path.join(dir,name)
		if os.path.isfile(fullpath):
			dir, filename = os.path.split(fullpath)
			resource, extension = os.path.splitext(filename)
			if extension == ".pot":
				containsPOT=True
			if filename == langPO:
				containsLangPO=True
		elif os.path.isdir(fullpath):
			recurseDirs(fullpath,langPO)
	
	if containsPOT and not containsLangPO:
		newFilePath=os.path.join(dir,langPO)
		print "Adding ",newFilePath
		file = open(newFilePath,"w") #Create empty lang.po file if not existing and pot exists
		file.write("")
		file.close()

recurseDirs(".", sys.argv[1] + ".po")