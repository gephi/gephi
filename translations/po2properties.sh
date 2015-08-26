#!/bin/bash

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
	path=`echo "$PWD" | sed 's,.*\/src\/main\/resources\/\(.*\)$,\1,' | sed 's,/,-,g'`

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

RecurseDirs "../modules"

