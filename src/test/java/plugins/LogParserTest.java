package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.logparser.LogParserGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.logparser.LogParserOutputPage;
import org.jenkinsci.test.acceptance.plugins.logparser.LogParserPublisher;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.jenkinsci.test.acceptance.junit.Resource;

import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void testing(){
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        j.configure();

        // sample use of the LogParserPublisher
        LogParserPublisher lpp = j.addPublisher(LogParserPublisher.class);
        lpp.setMarkOnUnstableWarning(true);
        lpp.setMarkOnBuildFail(true);
        lpp.setShowGraphs(true);
        lpp.setRule(LogParserPublisher.RuleType.GLOBAL, rules.get("some"));

        j.save();

        Build b = j.startBuild().waitUntilFinished();
        find(By.linkText("Parsed Console Output")).click();

        LogParserOutputPage p = new LogParserOutputPage(b);
        p.openFrameInWindow(LogParserOutputPage.LOGPARSERFRAME.CONTENT);
        p.restoreWindow();
        p.restoreWindow();
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
     * @param description The description of the new rule.
     * @param pathToFile The path to the rule file.
     */
    private void addLogParserRule(final String description, final String pathToFile){
        jenkins.configure();
        config.addParserConfig(description, pathToFile);
        jenkins.save();
    }

    /**
     * Adds serveral rules to the existing config.
     * @param rules Map of the rules. Key is the description and Value is the path.
     */
    private void addLogParserRules(final Map<String, String> rules) {
        jenkins.configure();
        for(Map.Entry<String, String> rule : rules.entrySet()) {
            config.addParserConfig(rule.getKey(), rule.getValue());
        }
        jenkins.save();
    }
}
