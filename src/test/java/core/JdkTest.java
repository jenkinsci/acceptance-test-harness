package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JdkInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.junit.Test;

import static org.junit.Assume.*;
import static org.hamcrest.CoreMatchers.*;

public class JdkTest extends AbstractJUnitTest {

    @Test
    public void autoinstallJdk() {
        final String login = System.getenv("ORACLE_LOGIN");
        final String passwd = System.getenv("ORACLE_PASSWORD");
        assumeThat(login, not(nullValue()));
        assumeThat(passwd, not(nullValue()));

        ToolInstallation.waitForUpdates(jenkins, JdkInstallation.class);

        jenkins.configure();
        JdkInstallation jdk = jenkins.getConfigPage().addTool(JdkInstallation.class);
        jdk.name.set("jdk_1.7.0");
        jdk.installVersion("jdk-7u11-oth-JPR");
        jenkins.save();

        jdk.setCredentials(login, passwd);

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("java -version");
        job.save();

        int tenMinutes = 600000;
        job.startBuild().waitUntilFinished(tenMinutes).shouldSucceed()
                .shouldContainsConsoleOutput("Installing JDK jdk-7u11-oth-JPR")
                .shouldContainsConsoleOutput("Downloading JDK from http://download.oracle.com")
        ;
    }
}
