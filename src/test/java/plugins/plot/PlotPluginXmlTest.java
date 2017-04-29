package plugins.plot;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.plot.*;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsLogger;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "plot"
})
public class PlotPluginXmlTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }


}
