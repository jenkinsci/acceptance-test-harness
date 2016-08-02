package core;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScriptTest extends AbstractJUnitTest {
    @Inject
    SlaveController slave;

    @Test
    public void execute_system_script() {
        String output = jenkins.runScript("println Jenkins.instance.displayName;");
        assertThat(output, is("Jenkins"));
    }

    @Test
    public void execute_system_script_on_slave() throws ExecutionException, InterruptedException {
        Slave s = slave.install(jenkins).get();
        String output = s.runScript("println 6 * 7");
        assertThat(output, is("42"));
    }
}
