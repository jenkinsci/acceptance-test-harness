package plugins;

import hudson.util.VersionNumber;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class RemotingPluginTest extends AbstractJUnitTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    /**
     * When installing a core component override plugin, we should see that plugin functioning.
     *
     * <p>
     * Run with environment variable: <tt>REMOTING_JPI=path/to/your-locally-build-remoting.hpi
     * <p>
     * We check this by:
     * <ol>
     *   <li>Testing the version of the plugin as declared installed
     *   <li>Making sure that the actual jar file in the system is replaced
     *  </ol>
     */
    @Test
    @WithPlugins({"remoting"})
    @Ignore // meant to be used to locally demonstrate JENKINS-41196 support
    public void test() throws IOException, InterruptedException {
        final String version = "3.12-SNAPSHOT";

        assertThat(trimSnapshot(jenkins.getPlugin("remoting").getVersion()), is(version));

        File jar = tmp.newFile();
        FileUtils.copyURLToFile(jenkins.url("jnlpJars/slave.jar"),jar);
        try (JarFile j = new JarFile(jar)) {// this is the copy of remoting jar
            Attributes main = j.getManifest().getMainAttributes();
            assertThat(main.getValue("Version"),is(version));
        }
    }

    /**
     * For version number like "1.0-SNAPSHOT (private-921e74fb-kohsuke)" trim off the whitespace and onward
     */
    private String trimSnapshot(VersionNumber v) {
        return v.toString().split(" ")[0];
    }
}