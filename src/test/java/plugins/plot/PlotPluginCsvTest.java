package plugins.plot;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;

/**
 * Created by lphex on 4/29/17.
 */
@WithPlugins({
        "matrix-project", // JENKINS-37545
        "plot"
})
public class PlotPluginCsvTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }
}
