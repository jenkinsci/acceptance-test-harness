/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.configuration_as_code.JcascManage;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Test;

@WithPlugins("configuration-as-code")
public class ConfigurationAsCodeTest extends AbstractJUnitTest {
    @Test
    public void loadAndReload() {
        final String EXPECTED_DESC = "JCasC populated description";

        JcascManage jm = new JcascManage(jenkins);
        jm.open();
        jm.configure(resource("/configuration_as_code/trivial.yaml").asFile().getAbsolutePath());

        assertThat(jenkins.open(), hasContent(EXPECTED_DESC));

        JenkinsConfig gc = jenkins.getConfigPage();
        gc.configure(() -> gc.setDescription("Changed"));
        assertThat(jenkins.open(), Matchers.not(hasContent(EXPECTED_DESC)));

        jm.open();
        jm.reload();
        assertThat(jenkins.open(), hasContent(EXPECTED_DESC));
    }
}
