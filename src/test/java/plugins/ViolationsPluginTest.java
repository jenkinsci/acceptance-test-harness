package plugins;

import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModule;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleBuild;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.violations.Violations;
import org.jenkinsci.test.acceptance.plugins.violations.ViolationsAction;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

@WithPlugins("violations")
public class ViolationsPluginTest extends AbstractJUnitTest {

    @Test
    public void freestyle() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyDir(resource("/violations_plugin"));
        job.addPublisher(Violations.class).config("fxcop").pattern("fxcop/*");
        job.save();

        Build build = job.startBuild().shouldSucceed();

        assertThat(job.action(ViolationsAction.class), hasViolations("fxcop", "2", "2"));
        assertThat(build.action(ViolationsAction.class), hasViolations("fxcop", "2", "2"));
    }

    @Test
    public void maven() {
        MavenInstallation.installSomeMaven(jenkins);

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/violations_plugin"));
        new Violations(job).config("fxcop").pattern("fxcop/*");
        job.save();

        job.startBuild().shouldSucceed();

        MavenModule module = job.module("gid$example");
        MavenModuleBuild moduleBuild = module.getLastBuild();

        assertThat(module.action(ViolationsAction.class), hasViolations("fxcop", "2", "2"));
        assertThat(moduleBuild.action(ViolationsAction.class), hasViolations("fxcop", "2", "2"));
    }

    private Matcher<ViolationsAction> hasViolations(final String kind, final String violations, final String files) {

        return new Matcher<ViolationsAction>("%s %s violations in %s files", violations, kind, files) {

            private final String pattern = "//td//a[@href='#%1$s' and text()='%1$s']/../../td[%2$d]";

            @Override
            public boolean matchesSafely(ViolationsAction item) {
                item.open();

                String actualViolations = find(by.xpath(pattern, kind, 2)).getText();
                String actualFiles = find(by.xpath(pattern, kind, 3)).getText();

                return violations.equals(actualViolations) && files.equals(actualFiles);
            }
        };
    }
}
