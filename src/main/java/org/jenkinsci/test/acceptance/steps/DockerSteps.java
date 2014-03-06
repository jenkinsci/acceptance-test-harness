package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.utils.process.CommandBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kohsuke Kawaguchi
 */
public class DockerSteps extends AbstractSteps {
    // contextual variables used from cucumber steps
    public final Map<String,DockerContainer> containers = new HashMap<>();
    public DockerContainer container;

    @Inject
    Docker docker;

    @Given("^a docker fixture \"([^\"]*)\"$")
    public void a_docker_fixture(String name) throws Throwable {
        Class<? extends DockerContainer> f = docker.findFixture(name);
        container = docker.start(f);
        containers.put(name,container);
    }

    @Then("^I can login via ssh( to fixture \"([^\"]*)\")?$")
    public void I_can_login_via_ssh(String _, String name) throws Throwable {
        ((SshdContainer)container(name)).sshWithPublicKey(new CommandBuilder("uname","-a"));
    }

    @After
    public void cleanUp() throws IOException, InterruptedException {
        for (Entry<String, DockerContainer> e : containers.entrySet()) {
            System.out.println("Shutting down: "+e.getKey());
            e.getValue().clean();
        }
    }

    /**
     * Obtains the contextual container.
     */
    public DockerContainer container(String name) {
        return name==null ? container : containers.get(name);
    }
}
