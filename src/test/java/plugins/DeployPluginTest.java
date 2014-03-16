package plugins;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.Tomcat7Container;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.deploy.DeployPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;

/**
 Feature: Auto-deployment to application server via deploy plugin
   In order to get rapid feedback on applications under the development,
   As a Jenkins user
   I want to automate the delivery of web applications
 */
@WithPlugins("deploy")
public class DeployPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    /**
     @native(docker)
     Scenario: Deploy sample webapp to Tomcat7
       Given I have installed the "deploy" plugin
       And a docker fixture "tomcat7"
       And a job
       When I configure the job
       And I add a shell build step
       """
         [ -d my-webapp ] || mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-webapp -DarchetypeArtifactId=maven-archetype-webapp
         cd my-webapp
         mvn install
       """
       And I deploy "my-webapp/target/*.war" to docker tomcat7 fixture at context path "test"
       And I save the job
       And I build the job
       Then the build should succeed
       And console output should match "to container Tomcat 7.x Remote"
       And docker tomcat7 fixture should show "Hello World!" at "/test/"

       When I configure the job
       And I change a shell build step to "cd my-webapp && echo '<html><body>Hello Jenkins</body></html>' > src/main/webapp/index.jsp && mvn install"
       And I save the job
       When I build the job
       Then the build should succeed
       And console output should match "Redeploying"
       And docker tomcat7 fixture should show "Hello Jenkins" at "/test/"
     */
    @Test
    public void deploy_sample_webapp_to_tomcat7() throws IOException {
        try (Tomcat7Container f = docker.start(Tomcat7Container.class)) {
            FreeStyleJob j = jenkins.jobs.create();
            j.configure();
            {
                j.addShellStep(resource("/deploy_plugin/build-war.sh"));
                DeployPublisher d = j.addPublisher(DeployPublisher.class);
                d.war.set("my-webapp/target/*.war");
                d.contextPath.set("test");
                d.container.select("Tomcat 7.x");
                d.user.set("admin");
                d.password.set("admin");
                d.url.set(f.getUrl().toExternalForm());
            }
            j.save();

            Build b = j.queueBuild().shouldSucceed();
            b.shouldContainsConsoleOutput("to container Tomcat 7.x Remote");

            URL url = new URL(f.getUrl(),"/test/");
            assertThat(IOUtils.toString(url.openStream()), is("Hello World!"));
        }
    }

}
