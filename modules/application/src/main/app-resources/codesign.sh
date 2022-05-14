#!/bin/bash

# Codesign a folder
function codesignDir {
  local dir="$1"

  # Codesign all all relevant files
  while IFS= read -r -d $'\0' libfile; do
    echo "Codesigning file $(basename "${libfile}")"
    codesign --verbose --entitlements src/main/resources/Entitlements.plist --deep --force --timestamp -i org.gephi --sign "$2" --options runtime $libfile
  done < <(find -E "$dir" -regex '.*\.(dylib|jnilib)' -print0)
}

# Codesign content of JARs
function codesignJarsInDir {
  local dir="$1"
  
  # Search for JAR files
  while IFS= read -r -d $'\0' file; do
    # Check if the JAR contains jnilib or dylib files
    jar tvf $file | grep "jnilib\|dylib" > /dev/null
    if [ $? -eq 0 ]
    then
        echo "Codesigning JAR file: $(basename "${file}")"

        # Set temp folder to unzip the JAR
        folder="$(dirname "${file}")/tmp"
        rm -Rf $folder

        # Unzip the JAR
        unzip -d $folder $file > /dev/null
        
        # Codesign all all relevant files
        codesignDir "$folder" "${2}"

        # Create updated JAR
        cd $folder
        zip -r "../$(basename "${file}")" . -x "*.DS_Store" > /dev/null
        cd - > /dev/null

        # Cleanup
        rm -Rf $folder
    fi
  done < <(find "$dir" -name "*.jar" -print0)
}

# Codesign a single file or folder
function codesignFile {
  local file="$1"
  echo "Codesigning $(basename "${file}")"
  codesign --verbose --entitlements src/main/resources/Entitlements.plist --deep --force --timestamp -i org.gephi --sign "$2" --options runtime $file
}

# Sign external JARs
for dir in "${1}/Contents/Resources/gephi/gephi/modules/ext" ; do
  codesignJarsInDir "$dir" "${2}"
done

# Sign native Netbeans libs
codesignDir "${1}/Contents/Resources/gephi/platform/modules/lib" "${2}"

# Sign JRE
codesignDir "${1}/Contents/PlugIns" "${2}"

# Sign launcher script
codesignFile "${1}/Contents/Resources/gephi/bin/gephi" "${2}"

# Sign app
codesignFile "${1}" "${2}"