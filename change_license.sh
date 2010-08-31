#!/bin/bash

# 
# Edit all project.properties contained in modules.
# 

old_license="\/gpl-3.0.txt"
new_license="\/agpl-3.0.txt"

files="$(find */*/project.properties)"
command="s/${old_license}/${new_license}/i"

sed -i ${command} ${files}

