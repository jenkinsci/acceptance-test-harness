package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Runs  XMPP / Jabber server container.
 */
@DockerFixture(id = "jabber", ports = 5222)
public class JabberContainer extends DockerContainer {

//    private static final String REPO_DIR = "/home/git/gitRepo.git";
//
//    public URL getUrl() throws IOException {
//        return new URL("http://localhost:" + port(22));
//    }
//
//    public String getRepoUrl() {
//        return "ssh://git@localhost:" + port(22) + REPO_DIR;
//    }
    public File getLogbotLogFile() throws IOException, InterruptedException {
        String timeString = getTimestampForLogfile();
        File logfileLocal = new File("/tmp/"+ timeString + ".txt");
        // .../test/.. is the hardcoded name of the conference room the bot joins
        String logfilePathRemote = "/.logbot/logs/test/"+ timeString + ".txt";

        super.cp(logfilePathRemote, "/tmp/");
        return logfileLocal;
    }

    private String getTimestampForLogfile() {
        Date today = new Date();
        return new SimpleDateFormat("yyyyMMdd").format(today);
    }

}
