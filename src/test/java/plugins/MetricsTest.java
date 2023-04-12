package plugins;

import org.jenkinsci.test.acceptance.AbstractPipelineTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.junit.Assert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class MetricsTest extends AbstractPipelineTest {

    private static final String METRICS_URL = "metrics/currentUser/%s";
    private static final String NOT_EXISTENT_NODE = "not_existent_node";

    private final Logger LOGGER = Logger.getLogger(MetricsTest.class.getName());


    @Test
    @WithPlugins("metrics")
    public void testMetrics() throws IOException {
        this.checkHealthcheck();
        this.checkPing();
        this.checkMetrics(0);

        // install a new plugin and create a job which will be queued forever
        jenkins.getPluginManager().installPlugins(new PluginSpec("parameter-separator", null));

        final Build b = this.createPipelineJobWithScript(scriptForPipeline()).startBuild();
        try {
            waitFor().withTimeout(30, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Pattern pattern = Pattern.compile(".*Jenkins.* doesn.*t have label .*" + NOT_EXISTENT_NODE + ".*");
                    return b.getConsole().contains("Waiting for next available executor") ||
                            pattern.matcher(b.getConsole()).find();
                }
            });
        } catch (TimeoutException ex) {
            LOGGER.log(Level.SEVERE, String.format("Waiting for available executor message not displayed. Console output: %s", b.getConsole()));
            throw ex;
        }

        this.checkHealthcheck();
        this.checkPing();
        this.checkMetrics(1);
    }

    @Override
    public String scriptForPipeline() {
        return String.format("node('%s') {}", NOT_EXISTENT_NODE);
    }

    private void checkHealthcheck() {
        jenkins.visit(String.format(METRICS_URL, "healthcheck"));

        Pattern pattern = Pattern.compile(".*\"plugins.*\":.*\\{.*\"healthy.*\":true,.*\"message.*\":.*\"No failed plugins.*\"\\}");
        assertThat(driver, hasContent(pattern));
    }

    private void checkPing() {
        jenkins.visit(String.format(METRICS_URL, "ping"));
        assertThat(driver, hasContent("pong"));
    }

    private void checkMetrics(final int numberQueuedJobs) {
        jenkins.visit(String.format(METRICS_URL, "metrics"));

        final String expectedMetric = String.format("\"jenkins.queue.size.value\":{\"value\":%d}", numberQueuedJobs);
        assertThat(driver, hasContent(expectedMetric));
    }

}