package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 Feature: Subversion support
   As a user
   I want to be able to check out source code from Subversion
 */
@WithPlugins("subversion")
public class SubversionPluginTest extends AbstractJUnitTest {

    /**
     Scenario: Run basic Subversion build
       Given I have installed the "subversion" plugin
       And a job
       When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project/"
       And I add a shell build step "test -d .svn"
       And I save the job
       And I build the job
       Then the build should succeed
       And console output should contain "test -d .svn"
     */
    @Test
    public void run_basic_subversion_build() {
        FreeStyleJob f = jenkins.jobs.create();
        f.configure();
        f.useScm(SubversionScm.class).url.set("https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project/");
        f.addShellStep("test -d .svn");
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }

    /**
     Scenario: Check out specified Subversion revision
       Given I have installed the "subversion" plugin
       And a job
       When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project@40156"
       And I save the job
       And I build the job
       Then the build should succeed
       And console output should contain "At revision 40156"
     */
    @Test
    public void checkout_specific_revision() {
        FreeStyleJob f = jenkins.jobs.create();
        f.configure();
        f.useScm(SubversionScm.class).url.set("https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project@40156");
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("At revision 40156");
    }

    /**
     Scenario: Always check out fresh copy
       Given I have installed the "subversion" plugin
       And a job
       When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project"
       And I select "Always check out a fresh copy" as a "Check-out Strategy"
       And I save the job
       And I build 2 jobs
       Then the build should succeed
       And console output should contain "Checking out https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project"
     */
    @Test
    public void always_checkout_fresh_copy() {
        FreeStyleJob f = jenkins.jobs.create();
        f.configure();
        f.useScm(SubversionScm.class).url.set("https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project");
        f.find(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select","Check-out Strategy"))
                .findElement(by.option("Always check out a fresh copy")).click();
        f.save();

        f.startBuild().shouldSucceed();

        f.startBuild().shouldSucceed()
            .shouldContainsConsoleOutput("Checking out https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project");
    }
}
