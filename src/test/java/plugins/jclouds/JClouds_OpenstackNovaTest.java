/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins.jclouds;

import static org.junit.Assume.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.jclouds.JCloudsCloud;
import org.jenkinsci.test.acceptance.plugins.jclouds.JCloudsSlaveTemplate;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Before;
import org.junit.Test;

@WithPlugins("jclouds-jenkins")
public class JClouds_OpenstackNovaTest extends AbstractJUnitTest {

    /* mandatory */
    private static final String ENDPOINT = System.getProperty(JClouds_OpenstackNovaTest.class.getName() + ".ENDPOINT");
    private static final String IDENTITY = System.getProperty(JClouds_OpenstackNovaTest.class.getName() + ".IDENTITY");
    private static final String CREDENTIAL = System.getProperty(JClouds_OpenstackNovaTest.class.getName() + ".CREDENTIAL");

    /* per test */
    private static final String HARDWARE_ID = System.getProperty(JClouds_OpenstackNovaTest.class.getName() + ".HARDWARE_ID");
    private static final String IMAGE_ID = System.getProperty(JClouds_OpenstackNovaTest.class.getName() + ".IMAGE_ID");
    private static final String IMAGE_NAME_REGEX = System.getProperty(JClouds_OpenstackNovaTest.class.getName() + ".IMAGE_NAME_REGEX");

    @Before
    public void setUp() {
        assumeNotNull(ENDPOINT);
        assumeNotNull(IDENTITY);
        assumeNotNull(CREDENTIAL);
    }

    @Test
    public void testConnection() {
        jenkins.configure();
        JCloudsCloud cloud = addCloud(jenkins.getConfigPage());
        cloud.testConnection();
        waitFor(driver, hasContent("Connection succeeded!"), 60);
    }

    @Test
    public void checkHardwareId() {
        assumeTrue(HARDWARE_ID != null || IMAGE_ID != null || IMAGE_NAME_REGEX != null);

        jenkins.configure();
        JCloudsCloud cloud = addCloud(jenkins.getConfigPage());
        JCloudsSlaveTemplate template = cloud.addSlaveTemplate();

        if(HARDWARE_ID != null) {
            template.hardwareId(HARDWARE_ID);
            template.checkHardwareId();
            waitFor(driver, hasContent("Hardware Id is valid."), 60);
        }

        if(IMAGE_ID != null) {
            template.imageId(IMAGE_ID);
            template.checkImageId();
            waitFor(driver, hasContent("Image Id is valid."), 60);
        }

        if (IMAGE_NAME_REGEX != null) {
            template.imageNameRegex(IMAGE_NAME_REGEX);
            template.checkImageNameRegex();
            waitFor(driver, hasContent("Image Name Regex is valid."), 60);
        }
    }

    private JCloudsCloud addCloud(JenkinsConfig config) {
        return config.addCloud(JCloudsCloud.class)
                .profile(Jenkins.createRandomName())
                .provider("openstack-nova")
                .endpoint(ENDPOINT)
                .identity(IDENTITY)
                .credential(CREDENTIAL)
                .instanceCap(1)
        ;
    }
}
