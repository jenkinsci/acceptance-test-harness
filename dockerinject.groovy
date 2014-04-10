import org.jenkinsci.test.acceptance.docker.Docker

import static junit.framework.TestCase.fail

//docker = "docker.io"
//bind Docker named "docker" to "docker.io"


String os = System.getProperty("os.name").toLowerCase();
String version = System.getProperty("os.version").toLowerCase();

// To ugly to check for all this files use lsb_release, if files are there this command is also there!!
// /etc/centos-release
// /etc/lsb-release
// /etc/redhat-release
// /etc/system-release
// /etc/os-release
//File release = new File("/etc/lsb-release");
// Fallback if lsb_release is not available
File issue = new File("/etc/issue");

private def dockerCmd(){
    docker = "docker"
}

private def dockerDotIoCmd(){
    docker = "docker.io"
}
private def StringBuffer[] getLSB_release(){
    def sout = new StringBuffer(), serr = new StringBuffer()
    def proc = 'lsb_release -a'.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(1000)
    //println "out> $sout err> $serr"
    def StringBuffer[] stdOutNErr = new StringBuffer[2]
    stdOutNErr[0] = sout
    stdOutNErr[1] = serr
    return stdOutNErr
}

private def StringBuffer[] getUname(){
    def sout = new StringBuffer(), serr = new StringBuffer()
    def proc = 'uname -a'.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(1000)
    //println "out> $sout err> $serr"
    def StringBuffer[] stdOutNErr = new StringBuffer[2]
    stdOutNErr[0] = sout
    stdOutNErr[1] = serr
    return stdOutNErr
}

private def macInjectStuff(){
    // add special mac stuff if needed
}

//MAC
if ( os.indexOf("mac") >= 0) {
    //do some mac specific binding
    dockerCmd()
    macInjectStuff()
//Windows
} else if (os.indexOf("win") >= 0) {
    fail("Sry not supported!");
//LINUX
} else if (os.indexOf("nux") >= 0 ) {

    String[] outErrStrings = getLSB_release()
    stdOut = outErrStrings[0]
    stdErr = outErrStrings[1]
    if (!stdOut.empty) {
        String lowerCaseStdOut = stdOut.toLowerCase()
        if (lowerCaseStdOut.indexOf("solydxk") >= 0 ||
                lowerCaseStdOut.indexOf("arch linux") >= 0 ||
                lowerCaseStdOut.indexOf("ubuntu") >= 0 ||
                lowerCaseStdOut.indexOf("fedora") >= 0 ||
                lowerCaseStdOut.indexOf("opensuse") >= 0
        ) {
            dockerCmd()
        } else if(lowerCaseStdOut.indexOf("debian") >=0 ){
            dockerDotIoCmd()
        } else {
            fail("Sry, you seem to be running Linux, the lsb_release command returned this:" +
                    stdOut + "but your distribution is not known to us! Don't know what to inject!")
        }
    // backup if lsb_release is not available by default centOS, redhat, arch linux, ...
    } else if (issue.exists()) {
        String content = "";
        issue.eachLine { line ->
            //println line //Debugging
            content = content + line.toLowerCase()
        }

        if (content.indexOf("solydxk") >= 0 ||
                content.indexOf("arch linux") >= 0 ||
                content.indexOf("ubuntu") >= 0 ||
                content.indexOf("fedora") >= 0 ||
                content.indexOf("opensuse") >= 0){
            dockerCmd()
        //Debian Stable
        }else if (content.indexOf("debian") >=0 ){
            dockerDotIoCmd()
        //default
        }else{
            fail("Sry, you seem to be running Linux but your distribution is not known!" +
                    " Don't know what to inject!")
        }

    } else {
        fail("Sry, you seem to be running Linux but your distribution is not known to us," +
                " and no lsb_release command nor a /etc/issue file was found! Don't know what to inject!")

    }
//UNIX / AIX / *BSDs
} else if( os.indexOf("nix") >= 0 || os.indexOf("aix") >= 0 || os.indexOf("bsd") >= 0 ){
    // They need special treatment no lsb_release and /etc/issue
    String[] outErrStrings = getUname()
    stdOut = outErrStrings[0]
    stdErr = outErrStrings[1]
    if (!stdOut.empty) {
        String lowerCaseStdOut = stdOut.toLowerCase()
        if (lowerCaseStdOut.indexOf("freebsd") >= 0 ||
                lowerCaseStdOut.indexOf("openbsd") >= 0 ||
                lowerCaseStdOut.indexOf("pcbsd") >= 0 ||
                lowerCaseStdOut.indexOf("netbsd") >= 0
        ) {
            fail("Sry, but your distribution is not supported by docker when this was written! Don't know what to inject!")

        } else {
            fail("Sry, you seem to be running Linux, the lsb_release command returned this:" +
                    stdOut + "but your distribution is not known to us! Don't know what to inject!")
        }

    } else {
        fail("Sry, you seem to be running some flavour of UNIX but your distribution is not known to us," +
                " and no lsb_release command nor a /etc/issue file was found! Don't know what to inject!")
    }

} else {
    //default
    fail("Sry, what System are you running? Don't know what to inject!")
}