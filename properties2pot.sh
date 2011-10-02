#!/bin/bash

#  Copyright 2008-2010 Gephi
#  Website : http://www.gephi.org
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
# http://gephi.org/about/legal/license-notice/
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

ROOT=`pwd`

function RecurseDirs
{
oldIFS=$IFS
IFS=$'\n'
for f in "$@"
do
if [[ $f == 'Bundle.properties' ]]; then
	PWD=`pwd`

	path=`echo "$PWD" | sed 's,.*\/src\/\(.*\)$,\1,' | sed 's,/,-,g'`

	#rm *.pot

	if [[ $path == org-* ]]; then
		# Duplicates Bundle.properties and remove specific lines
		ftmp=Bundle.properties.tmp
		cp $f $ftmp
		sed -i 's/\r$//' $ftmp
		sed -i '/OpenIDE-Module-Display-Category/ d' $ftmp
		sed -i '/OpenIDE-Module-Name/ d' $ftmp
		sed -i '/^org_gephi_branding_desktop_update_center/ d' $ftmp
		sed -i '/=\s*$/ d' $ftmp
		
		echo $path
		fname=${path}.pot
		# generate POT file from Bundle.properties
		msgcat $ftmp --properties-input --output-file=$fname

		if [[ -s $fname ]]; then
			#sed -i -l 2 '/msgid "TopTabComponent.logoLabel.text"\nmsgstr ""/ d' $fname
			
			#add header
			cp $fname tmp.txt
			cat ${ROOT}/pot-header.txt tmp.txt > $fname
			rm tmp.txt

			#check file
			msgfmt -c $fname
			rm messages.mo
		fi
		
		rm $ftmp
	fi

fi
if [[ -d "${f}" ]]; then
	cd "${f}"
	RecurseDirs $(ls -1 ".")
	cd ..
fi
done
IFS=$oldIFS
}

RecurseDirs .

