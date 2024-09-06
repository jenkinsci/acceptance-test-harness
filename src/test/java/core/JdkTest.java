package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JdkInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.junit.Test;

public class JdkTest extends AbstractJUnitTest {

    // This actually tests any installed JDK, not necessarily oracle.
    @Test
    @Native("java")
    public void usePreinstalledJdk() {
        String expectedVersion = localJavaVersion();

        JdkInstallation jdk = ToolInstallation.addTool(jenkins, JdkInstallation.class);
        jdk.name.set("preinstalled");
        jdk.useNative();
        jdk.getPage().save();

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
