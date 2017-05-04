package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.logparser.LogParserGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.logparser.LogParserPublisher;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

@WithPlugins("log-parser")
public class LogParserTest extends AbstractJUnitTest {

    // GlobalConfig for the LogParser-Plugin
    private LogParserGlobalConfig config;
    // Available Rules for the tests
    private Map<String, String> rules;

    @Before
    public void globalConfig() {
        config = new LogParserGlobalConfig(jenkins.getConfigPage());
        rules = new HashMap<>();
        // initialize a sample rule for the following test cases
        Resource sampleRule = resource("/logparser_plugin/rules/log-parser-rules-sample");
        rules.put("sampleRule", "" + sampleRule.url.getPath());
        addLogParserRules(rules);
    }

    /**
     * Test information for failed log parsing.
     */
    @Test
    public void invalidRulePath() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, "fail-job");

        // configure invalid route
        job.configure();
        LogParserPublisher lpp = job.addPublisher(LogParserPublisher.class);
        lpp.setRule(LogParserPublisher.RuleType.PROJECT, "invalidPath");
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        // check information on build overview
        build.open();
        WebElement tableRow = driver.findElement(By.xpath("//table/tbody/tr[3]"));
        WebElement icon = tableRow.findElement(By.xpath("td[1]/img"));
        assertThat(icon.getAttribute("src"), containsString("graph.png"));
        WebElement text = tableRow.findElement(By.xpath("td[2]"));
        assertThat(text.getText(), is("Log parsing has failed"));

        // check information in parsed console output
        driver.findElement(By.partialLinkText("Parsed Console Output")).click();
        WebElement output = driver.findElement(By.id("main-panel"));
        assertThat(output.getText(), containsString("ERROR: Failed to parse console log"));
    }

    /**
     * Check whether trend is visible
     */
    @Test
    public void trendVisible() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, "simple-job");

        job.configure(() -> {
            // sample use of the LogParserPublisher
            LogParserPublisher lpp = job.addPublisher(LogParserPublisher.class);
            lpp.setShowGraphs(true);
            lpp.setRule(LogParserPublisher.RuleType.GLOBAL, rules.get("sampleRule"));
        });

        // Trend is shown after second build
        job.startBuild().waitUntilFinished();
        job.startBuild().waitUntilFinished();

        // Check trend is visible
        job.open();
        WebElement trend = driver.findElement(By.className("test-trend-caption"));
        assertThat(trend.getText(), containsString("Log Parser Trend"));
        WebElement img = driver.findElement(By.xpath("//img[@src='logparser/trend']"));
        assertThat(img.getAttribute("alt"), containsString("[Log Parser Chart]"));
    }

    /**
     * Test case:
     * Check for a build to be marked as failed and another build to be marked as unstable.
     */
    @Test
    public void checkMarkedFailedAndUnstable() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        job.configure(() -> {
            job.addShellStep("echo marked as error");
            LogParserPublisher lpp = job.addPublisher(LogParserPublisher.class);
            lpp.setMarkOnBuildFail(true);
            lpp.setRule(resource("/logparser_plugin/rules/log-parser-rule-markings"));
        });
        job.startBuild().waitUntilFinished().shouldFail();

        job.configure(() -> {
            job.addShellStep("echo marked as warning");
            LogParserPublisher lpp = job.getPublisher(LogParserPublisher.class);
            lpp.setMarkOnBuildFail(false);
            lpp.setMarkOnUnstableWarning(true);
        });
        job.startBuild().waitUntilFinished().shouldBeUnstable();
    }

    /**
     * Adds a post-build-step to the job which prints out the content of the specified file.
     *
     * @param job The job where the post-build-step is added.
     * @param str The name of the file which shall be printed out.
     */
    private void catToConsole(final Job job, final String str) {
        job.addShellStep("cat " + str);
    }

    /**
     * Adds a new rule to the existing config.
     *
     * @param description The description of the new rule.
     * @param pathToFile  The path to the rule file.
     */
    private void addLogParserRule(final String description, final String pathToFile) {
        jenkins.configure();
        config.addParserConfig(description, pathToFile);
        jenkins.save();
    }

    /**
     * Adds a new rule to the existing config.
     *
     * @param description The description of the new rule.
     * @param resource    The {@link Resource} object of the rule file.
     */
    private void addLogParserRule(final String description, Resource resource) {
        addLogParserRule(description, resource.url.getPath());
    }

    /**
     * Adds serveral rules to the existing config.
     *
     * @param rules Map of the rules. Key is the description and Value is the path.
     */
    private void addLogParserRules(final Map<String, String> rules) {
        jenkins.configure();
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            config.addParserConfig(rule.getKey(), rule.getValue());
        }
        jenkins.save();
    }
}
