#!/bin/bash
function RecurseDirs
{
oldIFS=$IFS
IFS=$'\n'
for f in "$@"
do
if [[ $f == 'Bundle.properties' ]]; then
PWD=`pwd`

path=`echo "$PWD" | sed 's,.*\/src\/\(.*\)$,\1,' | sed 's,/,-,g'`

if [[ $path == org-* ]]; then
echo $path
msgcat $f --properties-input --output-file=${path}.pot
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

