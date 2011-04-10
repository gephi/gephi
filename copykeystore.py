#  Copyright 2008-2010 Gephi
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
import sys

import os
import re
import shutil


if (len(sys.argv) < 3):
    print "Usage:"
    print ">>python ./copystyore.py keystore.ks passphrase"
    sys.exit(1)

keystore = sys.argv[1]
passphrase = sys.argv[2]

project = open("nbproject/project.properties")
for line in project.readlines():
    m = re.match('project.[^=]*=(.*)$', line)
    if m:
        r = m.group(1).strip()
        if os.path.exists(r):
            print "Copying keystore in " + r
            private_dir = r + "/nbproject/private"
            if not os.path.exists(private_dir):
                os.makedirs(private_dir)
            shutil.copy2(keystore, private_dir + "/" + keystore)
            f = open(private_dir + "/private.properties", 'w')
            f.write("storepass=" + passphrase)
            f.close()
project.close()
