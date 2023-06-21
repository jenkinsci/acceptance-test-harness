/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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
import static org.hamcrest.Matchers.equalTo;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectExists;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.MailhogContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mailer.Mailer;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuild;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.maven.MavenProjectConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.WebElement;

@WithPlugins("maven-plugin")
@Category(DockerTest.class)
@WithDocker
@Since( "2.204.6" )
public class MavenPluginTest extends AbstractJUnitTest {

    private static final String GENERATE = "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B";

    @Inject
    DockerContainerHolder<MailhogContainer> mailhogProvider;

    @Test
    public void autoinstall_maven_for_freestyle_job() {
        MavenInstallation.installMaven(jenkins, "maven_3.6.3", "3.6.3");

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenBuildStep step = job.addBuildStep(MavenBuildStep.class);
        step.version.select("maven_3.6.3");
        step.targets.set("-version");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("Apache Maven 3.6.3");
    }

    @Test
    public void autoinstall_maven3_for_freestyle_job() {
        MavenInstallation.installMaven(jenkins, "maven_3.6.3", "3.6.3");

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenBuildStep step = job.addBuildStep(MavenBuildStep.class);
        step.version.select("maven_3.6.3");
        step.targets.set("-version");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("Apache Maven 3.6.3");
    }

    @Test
    public void use_local_maven_repo() {
        MavenInstallation.installSomeMaven(jenkins);

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MavenBuildStep step = job.addBuildStep(MavenBuildStep.class);
        step.targets.set(GENERATE);
        step.useLocalRepository();
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("-Dmaven.repo.local=([^\\n]*)[/\\\\].repository");
    }

    @Test
    public void set_maven_options() {
        MavenInstallation.installSomeMaven(jenkins);

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/maven_plugin/multimodule/"));
        job.goals.set("clean");
        job.options("-showversion");
        job.save();

        job.startBuild().waitUntilFinished().shouldContainsConsoleOutput(" Runtime Environment ");
    }

    @Test
    public void set_global_maven_options() {
        MavenInstallation.installSomeMaven(jenkins);
        jenkins.configure();
        new MavenProjectConfig(jenkins.getConfigPage()).opts.set("-showversion");
        jenkins.save();

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/maven_plugin/multimodule/"));
        job.goals.set("clean");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput(" Runtime Environment ");
    }

    @Test
    @Issue("JENKINS-10539")
    @Since("1.527")
    public void preserve_backslash_in_property() {
        MavenInstallation.installSomeMaven(jenkins);

        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.configure();
        job.addParameter(StringParameter.class).setName("CMD");
        job.addParameter(StringParameter.class).setName("PROPERTY");

        MavenBuildStep step = job.addBuildStep(MavenBuildStep.class);
        step.targets.set(GENERATE + " -Dcmdline.property=$CMD");
        step.properties("property.property=$PROPERTY", true);
        job.save();

        Map<String, String> params = new HashMap<>();
        params.put("CMD", "\"C:\\\\System\"");
        params.put("PROPERTY", "C:\\Windows");
        job.startBuild(params).shouldSucceed()
                .shouldContainsConsoleOutput("cmdline.property=C:\\\\System")
                .shouldContainsConsoleOutput("property.property=C:\\\\Windows")
        ;
    }

    @Test
    public void build_multimodule() {
        MavenInstallation.installSomeMaven(jenkins);

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/maven_plugin/multimodule/"));
        job.goals.set("package");
        job.save();

        job.startBuild().shouldSucceed()
                .shouldContainsConsoleOutput("Building root 1.0")
                .shouldContainsConsoleOutput("Building module_a 2.0")
                .shouldContainsConsoleOutput("Building module_b 3.0")
        ;

        assertHasModule(job, "gid$root");
        assertHasModule(job, "gid$module_a");
        assertHasModule(job, "gid$module_b");
    }

    @Test @Issue({"JENKINS-20209", "JENKINS-21045"})
    public void send_mail() throws Exception {
        MavenInstallation.installSomeMaven(jenkins);
        MailhogContainer mailhog = mailhogProvider.get();

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        Mailer mailer = job.addBuildSettings(Mailer.class);
        mailer.recipients.set("root@example.com");
        job.save();

        job.startBuild().shouldFail();

        mailhog.assertMail(
                Pattern.compile("Build failed in Jenkins: .* #1"),
                "root@example.com",
                Pattern.compile(job.name)
        );
    }

    private void assertHasModule(MavenModuleSet job, String name) {
        assertThat(job.module(name), pageObjectExists());

        MavenBuild build = job.getLastBuild();
        assertThat(build.module(name), pageObjectExists());

        job.visit("modules");
        WebElement webElement = find(by.xpath("//a[@href='%s/']", name));
        webElement.click();
        // a menu pops out with how selenium clicks this, an actual user will not have an issue with this
        // clicking a second time fixes it
        if (!driver.getCurrentUrl().equals(job.module(name).url.toExternalForm())) {
            webElement.click();
        }
        assertThat(driver.getCurrentUrl(), equalTo(job.module(name).url.toExternalForm()));

        build.open();
        find(by.xpath("//a[@href='%s/']", name)).click();
        assertThat(driver.getCurrentUrl(), equalTo(build.module(name).url.toExternalForm()));
    }
}
