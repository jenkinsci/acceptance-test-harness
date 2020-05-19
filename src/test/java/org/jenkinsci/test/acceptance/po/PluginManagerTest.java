package org.jenkinsci.test.acceptance.po;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.junit.Test;

public class PluginManagerTest extends AbstractJUnitTest {

    @Test
    public void installAVerySpecificPluginByHpiFile() throws IOException, URISyntaxException {
        final File plugin = new File(getClass().getResource("ant-1.0.hpi").toURI());
        jenkins.getPluginManager().installPlugin(plugin);
    }
}
