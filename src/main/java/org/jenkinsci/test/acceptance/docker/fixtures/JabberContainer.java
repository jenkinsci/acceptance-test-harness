package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;

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
        File logfileLocal = new File("/tmp/logbot.log");
        // .../test/.. is the hardcoded name of the conference room the bot joins
        String logfilePathRemote = "/.logbot/logs/test/"+ getTimestampForLogfile() + ".txt";

        super.cp(logfilePathRemote,logfileLocal);
        return logfileLocal;
    }

    private String getTimestampForLogfile() {
        Date today = new Date();
        String timestamp = new SimpleDateFormat("yyyyMMdd").format(today);
        return timestamp;
    }

}