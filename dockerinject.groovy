import org.jenkinsci.test.acceptance.docker.Docker

import static junit.framework.TestCase.fail


/*
##################################################################
### If you want to use this groovy guice injection script,     ###
### then define a System Environment Variable called "CONFIG", ###
### with the path to this file as the argument!                ###
##################################################################
 */

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

/**
 * sets the named docker variable for Guice to "docker"
 */
private def dockerCmd(){
    docker = "docker"
}

/**
 * sets the named docker variable for Guice to "docker.io"
 */
private def dockerDotIoCmd(){
    docker = "docker.io"
}

/**
 * Use the lsb_release command to figure out the distribution you are using.
 *
 * @return StringBuffer Array with first index is Standard Output and second index is Standard ERROR
 */
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
/**
 * Use the /etc/issue file to figure out the distribution you are using.
 *
 * @return StringBuffer Array with first index is Standard Output and second index is Standard ERROR
 */
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

/**
 * Use this Funtions to handle MAC specific injection stuff for Guice
 */
private def macInjectStuff(){
    // add special mac stuff if needed
}

/**
 * This function tries to figure out what distribution you are running if you feed it with a all lower case string
 * that you gather via command or by reading a file
 *
 * @param toCheck String that should be all lower case and that gets checked
 * @return true if we were successful, false if we failed
 */
private def Boolean linuxDistributionStuff(String toCheck){
    Boolean boolFlag = false

    if (toCheck.indexOf("solydxk") >= 0 ||
            toCheck.indexOf("arch linux") >= 0 ||
            toCheck.indexOf("ubuntu") >= 0 ||
            toCheck.indexOf("fedora") >= 0 ||
            toCheck.indexOf("opensuse") >= 0
    ) {
        dockerCmd()
        boolFlag = true
    } else if(toCheck.indexOf("debian") >=0 ){
        dockerDotIoCmd()
        boolFlag = true
    } else {
        boolFlag = false
    }

    return boolFlag
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
        Boolean success = linuxDistributionStuff(lowerCaseStdOut)
        if (!success){
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
        Boolean success = linuxDistributionStuff(content)
        if (!success){
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