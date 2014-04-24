package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;

import com.google.inject.Inject;

@WithPlugins("ownership")
public class OwnershipPluginTest extends AbstractJUnitTest {

    @Inject
    private SlaveController slaves;

    @Test
    public void record() throws Exception {
        Slave slave = slaves.install(jenkins).get();
        jenkins.jobs.create();

        throw new RuntimeException();
    }
}
