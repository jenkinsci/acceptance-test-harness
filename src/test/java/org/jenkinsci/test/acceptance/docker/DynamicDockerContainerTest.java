package org.jenkinsci.test.acceptance.docker;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.core.Is.is;

/**
 * Tests the 
 * @author jnord
 *
 */
public class DynamicDockerContainerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    @Test
    public void testFiltering() throws Exception {
        String source = "# A dummy Dockerfile\n" + 
                        "#\n" + 
                        "\n" + 
                        "FROM ubuntu\n" + 
                        "\n" + 
                        "# install SSHD\n" + 
                        "RUN echo '@@DOCKER_HOST@@' >> /tmp/foo\n" + 
                        "RUN echo $(date) >> /tmp/the_time\n"; 
        String expected = "# A dummy Dockerfile\n" + 
                          "#\n" + 
                          "\n" + 
                          "FROM ubuntu\n" + 
                          "\n" + 
                          "# install SSHD\n" + 
                          "RUN echo '" + DockerImage.getDockerHost()+ "' >> /tmp/foo\n" + 
                          "RUN echo $(date) >> /tmp/the_time\n";
        File f = tmp.newFile();
        FileUtils.write(f, source, StandardCharsets.UTF_8);
        DynamicDockerContainer ddc = new DynamicDockerContainer();
        ddc.process(f);
        String actual = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        assertThat("File not transformed correctly", actual, is(expected));
    }

}
