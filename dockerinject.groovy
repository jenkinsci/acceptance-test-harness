import org.jenkinsci.test.acceptance.docker.Docker

//docker = "docker.io"
//bind Docker named "docker" to "docker.io"


String os = System.getProperty("os.name").toLowerCase();
String version = System.getProperty("os.version").toLowerCase();

File release = new File("/etc/lsb-release");

if ( os.indexOf("mac") >= 0) {
    //do some mac specific binding
} else if (os.indexOf("win") >= 0) {
    System.out.println("Sry not supported!");
    System.exit(1);
//UNIX / LINUX / AIX
} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0 ) {
    //do some linux stuff
    //System.out.println(os + " - " +version); //Debugging
    if(release.exists()) {
        String content = "";
        release.eachLine { line ->
            //println line //Debugging
            content = content + line.toLowerCase()
        }

        //System.out.println(content); //Debugging
        //Archlinux und SolydXK(Debian Testing "Rolling" Distribution)
        if (content.indexOf("solydxk") >= 0 || content.indexOf("archlinux") >= 0 || content.indexOf("ubuntu") >= 0 || content.indexOf("opensuse") >= 0){
            docker = "docker"
        //Debian Stable
        }else if (content.indexOf("debian") >=0 ){
            docker = "docker.io"
        //default
        }else{
            docker = "docker"
        }

    }

} else {
    //default
    System.out.println("Sry, what System are you running? Don't know what to inject!")
    System.exit(1);
}