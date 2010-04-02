#!/bin/bash
function RecurseDirs
{
oldIFS=$IFS
IFS=$'\n'
for f in "$@"
do
if [ $f == 'Bundle.properties' ]; then
echo `pwd`
msgcat $f --properties-input --output-file=en.po
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

