#!/bin/bash

# The selection used by update-alternatives for each java version
if [ "$1" == "11" ]; then
    # Java 11
    selection="1"
    runcommand="Depending on the Jenkins version and your tests you will likely need some extra params. See https://github.com/jenkinsci/acceptance-test-harness/blob/master/docs/JAVA11.md"
else
    # Java 8 is set as second option, default
    selection="2"
fi

# Same command for both verions, no modules, libraries or enable-java anymore since 2.164
runcommand=${runcommand}"\n./run.sh firefox latest -DforkCount=1 -Dmaven.test.failure.ignore=true -B -Dtest=..."

# Change all java programs, not only java and javac
# List of programs you can find at the etc/alternatives directory (due to java8 and java11)
programs="appletviewer
appletviewer.1.gz
extcheck
extcheck.1.gz
idlj
idlj.1.gz
jaotc
jar
jar.1.gz
jarsigner
jarsigner.1.gz
java
java.1.gz
javac
javac.1.gz
javadoc
javadoc.1.gz
javah
javah.1.gz
javap
javap.1.gz
jcmd
jcmd.1.gz
jconsole
jconsole.1.gz
jdb
jdb.1.gz
jdeprscan
jdeps
jdeps.1.gz
jexec
jexec-binfmt
jhat
jhat.1.gz
jhsdb
jimage
jinfo
jinfo.1.gz
jjs
jjs.1.gz
jlink
jmap
jmap.1.gz
jmod
jps
jps.1.gz
jrunscript
jrunscript.1.gz
jsadebugd
jsadebugd.1.gz
jshell
jstack
jstack.1.gz
jstat
jstat.1.gz
jstatd
jstatd.1.gz
keytool
keytool.1.gz
native2ascii
native2ascii.1.gz
orbd
orbd.1.gz
pack200
pack200.1.gz
policytool
policytool.1.gz
rmic
rmic.1.gz
rmid
rmid.1.gz
rmiregistry
rmiregistry.1.gz
schemagen
schemagen.1.gz
serialver
serialver.1.gz
servertool
servertool.1.gz
tnameserv
tnameserv.1.gz
unpack200
unpack200.1.gz
wsgen
wsgen.1.gz
wsimport
wsimport.1.gz
xjc
xjc.1.gz"

# Set to the right java version (if the program doesn't exist in this java version, it's not changed)
for program in $programs
do
    # Change the links without showing the choices nor the programs not found
    echo $selection | update-alternatives --config $program > /dev/null 2> /dev/null
done

echo
#To avoid us getting confused whether this commands are executed or are just documentation when looking at the logs
echo -------------------- INFORMATION --------------------
echo Running on...
java -version
echo
javac -version
echo
echo Start running tests with...
echo
echo 'eval $(./vnc.sh)'
echo
echo -e $runcommand
echo
echo ------------------ END INFORMATION ------------------
echo
