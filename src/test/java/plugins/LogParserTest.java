package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.logparser.LogParserGlobalConfig;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@WithPlugins("log-parser")
public class LogParserTest extends AbstractJUnitTest {

    private LogParserGlobalConfig config;

    @Before
    public void globalConfig() {
        config = new LogParserGlobalConfig(jenkins.getConfigPage());
    }

    @Test
    public void testing(){
        Map<String, String> rules = new HashMap<>();
        rules.put("des1", "path1");
        rules.put("des2", "path2");
        addLogParserRule(rules);
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        j.configure();
        Resource res = resource("/warnings_plugin/warningsAll.txt");
        if (res.asFile().isDirectory()) {
            j.copyDir(res);
        }
        else {
            j.copyResource(res);
        }
        catToConsole(j, "warningsAll.txt");
        j.save();

        j.startBuild().waitUntilFinished();
        String s = "";


    }

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
    private void addLogParserRule(final Map<String, String> rules) {
        jenkins.configure();
        for(Map.Entry<String, String> rule : rules.entrySet()) {
            config.addParserConfig(rule.getKey(), rule.getValue());
        }
        jenkins.save();
    }
}
