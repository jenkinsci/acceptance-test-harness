#!/bin/bash

# The selection used by update-alternatives for each java version
if [ "$1" == "11" ]; then
    # Java 11
    selection="1"
    runcommand="env JAVA_OPTS=\"${JAVA_OPTS} -p /home/ath-user/jdk11-libs/jaxb-api.jar:/home/ath-user/jdk11-libs/javax.activation.jar --add-modules java.xml.bind,java.activation -cp /home/ath-user/jdk11-libs/jaxb-impl.jar:/home/ath-user/jdk11-libs/jaxb-core.jar\" JENKINS_OPTS=\"--enable-future-java\" ./run.sh firefox latest -DforkCount=1 -Dmaven.test.failure.ignore=true -B -Dtest=..."
else
    # Java 8 is set as second option, default
    selection="2"
    runcommand="./run.sh firefox latest -DforkCount=1 -Dmaven.test.failure.ignore=true -B -Dtest=..."
fi

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
echo Running on...
java -version
echo
javac -version
echo
echo Start running tests with...
echo 'eval $(./vnc.sh)'
echo
echo $runcommand
echo