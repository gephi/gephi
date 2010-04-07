#!/bin/bash
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

if [[ $path == org-* ]]; then
echo $path
fname=${path}.pot
# generate POT file from Bundle.properties
#msginit --input=$f --properties-input --output-file=gephi-${path}.pot
msgcat $f --properties-input --output-file=$fname

#add header
head -q $fname > tmp.txt
head -q ${ROOT}/pot-header.txt -n 18 tmp.txt > $fname
rm tmp.txt
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

