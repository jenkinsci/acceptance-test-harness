package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.cobertura.CoberturaAction;
import org.jenkinsci.test.acceptance.plugins.cobertura.CoberturaPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins("cobertura")
public class CoberturaPluginTest extends AbstractJUnitTest {

    @Test
    public void record_covertura_coverage_report() {
        FreeStyleJob j = setupJob();

        Build b = j.startBuild().waitUntilFinished();
        assertThat(b, hasAction("Coverage Report"));
        assertThat(j, hasAction("Coverage Report"));
    }

    private FreeStyleJob setupJob() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        {
            j.copyResource(resource("/cobertura_plugin/coverage.xml"));
            CoberturaPublisher c = j.addPublisher(CoberturaPublisher.class);
            c.reportFile.set("coverage.xml");
        }
        j.save();
        return j;
    }

    @Test
    public void view_cobertura_coverage_report() {
        FreeStyleJob j = setupJob();

        Build b = j.startBuild().waitUntilFinished();
        CoberturaAction a = new CoberturaAction(b);

        assertThat(a.getPackageCoverage(), is(100));
        assertThat(a.getFilesCoverage(), is(50));
        assertThat(a.getClassesCoverage(), is(31));
        assertThat(a.getMethodsCoverage(), is(23));
        assertThat(a.getLinesCoverage(), is(16));
        assertThat(a.getConditionalsCoverage(), is(10));
    }
}
