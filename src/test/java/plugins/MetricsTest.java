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

import org.jenkinsci.test.acceptance.AbstractPipelineTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class MetricsTest extends AbstractPipelineTest {

    private static final String METRICS_URL = "metrics/currentUser/%s";
    private static final String NOT_EXISTENT_NODE = "not_existent_node";

    private final Logger LOGGER = Logger.getLogger(MetricsTest.class.getName());


    @Test
    @WithPlugins({"metrics", "parameter-separator"})
    public void testMetrics() throws IOException {
        this.checkHealthcheck();
        this.checkPing();
        this.checkMetrics(0);

        final Build b = this.createPipelineJobWithScript(scriptForPipeline()).startBuild();
        try {
            waitFor().withTimeout(Duration.ofSeconds(30)).until(new Callable<Boolean>() {
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
