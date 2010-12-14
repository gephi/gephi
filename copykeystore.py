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