package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.TestActivation;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JdkInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.junit.Test;

public class JdkTest extends AbstractJUnitTest {

    @Test @TestActivation({"ORACLE_LOGIN", "ORACLE_PASSWORD"})
    public void autoinstallJdk() {
        final String login = System.getenv("ORACLE_LOGIN");
        final String passwd = System.getenv("ORACLE_PASSWORD");

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

    // This actually tests any installed JDK, not necessarily oracle.
    @Test @Native("java")
    public void usePreinstalledJdk() {
        String expectedVersion = localJavaVersion();

        jenkins.configure();
        JdkInstallation jdk = jenkins.getConfigPage().addTool(JdkInstallation.class);
        jdk.name.set("preinstalled");
        jdk.useNative();
        jenkins.save();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("java -version");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput(expectedVersion);
    }

    private String localJavaVersion() {
        return jenkins.runScript("'java -version'.execute().text");
    }
}
