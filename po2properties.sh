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
#lang=`expr match "$f" '\(\.po\)'`
#SUBSTRING=`expr match "$f" '.*_\(\.po\)_.*' `
if [[ $f == *\.po ]]; then
	PWD=`pwd`
	path=`echo "$PWD" | sed 's,.*\/src\/\(.*\)$,\1,' | sed 's,/,-,g'`

	if [[ $f == *\.po ]]; then
		lang=`expr match "$f" '\(.*\).po' ` 
		fname=Bundle_${lang}.properties
		echo $path":" $f "->" $fname
		# generate Bundle_LG.properties file from PO
		msgcat $f --properties-output --output-file=$fname

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

