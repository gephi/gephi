#!/bin/bash
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

