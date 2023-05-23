package plugins;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.Tomcat10Container;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.deploy.DeployPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.jenkinsci.utils.process.CommandBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.inject.Inject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@WithPlugins("deploy")
@Category(DockerTest.class)
@WithDocker
public class DeployPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<Tomcat10Container> docker;

    @Test
    @Native("bash")
    @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {"admin", "tomcat"}, id = "tomcat")
    public void deploy_sample_webapp_to_tomcat10() throws IOException, InterruptedException {
        if (SystemUtils.IS_OS_WINDOWS) {
            // TODO move somewhere else...
            String path = new CommandBuilder("where.exe", "bash.exe").popen().asText().trim();
            // where will return all matches and we only want the first.
            path = path.replaceAll("\r\n.*", "");
            JenkinsConfig conf = jenkins.getConfigPage();
            JenkinsConfig cp = jenkins.getConfigPage();
            cp.configure();
            cp.setShell(path);
            cp.save();
        }
        Tomcat10Container f = docker.get();

        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        ShellBuildStep s;
        {
            s = j.addShellStep(resource("/deploy_plugin/build-war.sh"));
            DeployPublisher d = j.addPublisher(DeployPublisher.class);
            d.war.set("my-webapp/target/*.war");
            d.contextPath.set("test");
            d.useContainer("Tomcat 7.x Remote", "Tomcat 7.x");
            d.setCredentials("tomcat");
            d.url.set(f.getUrl().toExternalForm());
        }
        j.save();

        Build b = j.startBuild().shouldSucceed();
        b.shouldContainsConsoleOutput("to container Tomcat 7.x Remote");

        assertThat(readText(f), containsString("Hello World!"));

        j.configure();
        s.command("cd my-webapp && echo '<html><body>Hello Jenkins</body></html>' > src/main/webapp/index.jsp && mvn install");
        j.save();

        b = j.startBuild().shouldSucceed();
        b.shouldContainsConsoleOutput("Redeploying");
        assertThat(readText(f), containsString("Hello Jenkins"));
    }

    private String readText(Tomcat10Container f) throws IOException {
        URL url = new URL(f.getUrl(), "/test/");
        return IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
    }
}
