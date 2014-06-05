package core;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Feature: Adds Scripting support
 */
public class ScriptTest extends AbstractJUnitTest {
    @Inject
    SlaveController slave;

    /**
     * Scenario: Execute system script
     * When I execute system script
     * """
     * println Jenkins.instance.displayName;
     * """
     * Then the system script output should match "Jenkins"
     */
    @Test
    public void execute_system_script() {
        String output = jenkins.runScript("println Jenkins.instance.displayName;");
        assertThat(output, is("Jenkins"));
    }

    /**
     * Scenario: Execute system script on slave
     * Given a slave named "my_slave"
     * When I execute system script on "my_slave"
     * """
     * println 6 * 7;
     * """
     * Then the system script output should match "42"
     */
    @Test
    public void execute_system_script_on_slave() throws ExecutionException, InterruptedException {
        Slave s = slave.install(jenkins).get();
        String output = s.runScript("println 6 * 7");
        assertThat(output, is("42"));
    }
}
