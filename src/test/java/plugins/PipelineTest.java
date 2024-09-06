/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.AbstractPipelineTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Tests a complete pipeline flow:
 * 1- Checkout repo from github
 * 2- Execute tests
 * 3- Generate javadoc
 * 4- Assert results have been collected
 *
 */
@WithPlugins({"git", "junit", "javadoc"})
public class PipelineTest extends AbstractPipelineTest {

    @Before
    public void setup() {
        MavenInstallation.installMaven(jenkins, "M3", "3.9.4");
    }

    @Test
    public void testCompletePipeline() {
        final WorkflowJob job = createPipelineJobWithScript(scriptForPipeline());
        final Build b = job.startBuild().shouldBeUnstable(); // Successful build with test failures

        this.assertTestResult(b);
        this.assertJavadoc(job);
    }

    @Override
    public String scriptForPipeline() {
        if (SystemUtils.IS_OS_UNIX) {
            return "node {\n" + "  git url: '"
                    + this.githubRepoURL() + "'\n" + "  def mvnHome = tool 'M3'\n"
                    + "  cbEnv = [\"PATH+MVN=${mvnHome}/bin\", \"MAVEN_HOME=${mvnHome}\"]\n"
                    + "  \n"
                    + "  withEnv(cbEnv) {\n"
                    + "      sh \"mvn test\"\n"
                    + "      junit 'target/surefire-reports/TEST-io.jenkins.tools.MainTest.xml'\n"
                    + "      \n"
                    + "      sh \"mvn javadoc:javadoc -f pom.xml\"\n"
                    + "      step([$class: 'JavadocArchiver', javadocDir: 'target/reports/apidocs', keepAll: false])\n"
                    + "  }"
                    + "}";
        } else {
            // Windows
            return "node {\n" + "  git url: '"
                    + this.githubRepoURL() + "'\n" + "  def mvnHome = tool 'M3'\n"
                    + "  cbEnv = [\"PATH+MVN=${mvnHome}/bin\", \"MAVEN_HOME=${mvnHome}\"]\n"
                    + "  \n"
                    + "  withEnv(cbEnv) {\n"
                    + "      bat \"mvn test\"\n"
                    + "      junit 'target/surefire-reports/TEST-io.jenkins.tools.MainTest.xml'\n"
                    + "      \n"
                    + "      bat \"mvn javadoc:javadoc -f pom.xml\"\n"
                    + "      step([$class: 'JavadocArchiver', javadocDir: 'target/reports/apidocs', keepAll: false])\n"
                    + "  }"
                    + "}";
        }
    }

    private String githubRepoURL() {
        return "https://github.com/jenkinsci/hello-world-maven-builder";
    }

    private void assertTestResult(final Build b) {
        b.openStatusPage();

        final WebElement testResultLink = getElement(by.link("Test Result"));
        assertNotNull("Test Result link not found", testResultLink);
        assertThat(driver, hasContent("2 failures"));

        testResultLink.click();
        assertThat(driver, hasContent("io.jenkins.tools.MainTest.testApp"));
    }
}
