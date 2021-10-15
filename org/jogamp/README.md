## How to manually install JOGL artifacts

From the root of this repository,

Download the JARs from the [remote repository](https://jogamp.org/deployment/archive/rc/v2.4.0-rc-20210111/) and then install them using the following commands

- Set path where all the JARs are saved and the version

		export JOGL_PATH= ...
		export JOGL_VERSION= ...

- Install JARs one by one

		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/gluegen-rt.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dversion=$JOGL_VERSION -Dpackaging=jar -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/gluegen-rt-natives-linux-amd64.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-linux-amd64 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/gluegen-rt-natives-linux-i586.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-linux-i586 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/gluegen-rt-natives-macosx-universal.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-macosx-universal -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/gluegen-rt-natives-windows-amd64.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-windows-amd64 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/gluegen-rt-natives-windows-i586.jar -DgroupId=org.jogamp.gluegen -DartifactId=gluegen-rt -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-windows-i586 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/jogl-all.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=$JOGL_VERSION -Dpackaging=jar -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/jogl-all-natives-linux-amd64.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-linux-amd64 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/jogl-all-natives-linux-i586.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-linux-i586 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/jogl-all-natives-macosx-universal.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-macosx-universal -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/jogl-all-natives-windows-amd64.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-windows-amd64 -DcreateChecksum=true -DlocalRepositoryPath=.
		mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$JOGL_PATH/jogl-all-natives-windows-i586.jar -DgroupId=org.jogamp.jogl -DartifactId=jogl-all -Dversion=$JOGL_VERSION -Dpackaging=jar -Dclassifier=natives-windows-i586 -DcreateChecksum=true -DlocalRepositoryPath=.

- And finally rename the `maven-metadata-local.xml` file to look like rather like a remote repository

 		mv org/jogamp/jogl/jogl-all/maven-metadata-local.xml org/jogamp/jogl/jogl-all/maven-metadata.xml
 		mv org/jogamp/jogl/jogl-all/maven-metadata-local.xml.md5 org/jogamp/jogl/jogl-all/maven-metadata.xml.md5
 		mv org/jogamp/jogl/jogl-all/maven-metadata-local.xml.sha1 org/jogamp/jogl/jogl-all/maven-metadata.xml.sha1
 		mv org/jogamp/gluegen/gluegen-rt/maven-metadata-local.xml org/jogamp/gluegen/gluegen-rt/maven-metadata.xml
 		mv org/jogamp/gluegen/gluegen-rt/maven-metadata-local.xml.md5 org/jogamp/gluegen/gluegen-rt/maven-metadata.xml.md5
 		mv org/jogamp/gluegen/gluegen-rt/maven-metadata-local.xml.sha1 org/jogamp/gluegen/gluegen-rt/maven-metadata.xml.sha1