#!/bin/bash

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

