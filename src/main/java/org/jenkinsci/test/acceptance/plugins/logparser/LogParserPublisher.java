package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.po.*;

/**
 * Helperclass for configuring the logparser plugin.
 *
 * @author Michael Engel
 */
@Describable("Console output (build log) parsing")
public class LogParserPublisher extends AbstractStep implements PostBuildStep {

    // The available types of rules of the log-parser
    public enum RuleType {
        PROJECT("Use project rule"), GLOBAL("Use global rule");
        private final String projecttype;

        RuleType(String projecttype) {
            this.projecttype = projecttype;
        }

        ;

        public String getProjecttype() {
            return projecttype;
        }
    }

    // available controls of the log-parser post-build-step
    private final Control controlMarkOnUnstableWarning = control("unstableOnWarning");
    private final Control controlMarkOnBuildFail = control("failBuildOnError");
    private final Control controlShowGraphs = control("showGraphs");
    private final Control controlRuleType = control("useProjectRule");

    /**
     * Constructor.
     * Should only be instantiated by addPublisher(...) of a job
     */
    public LogParserPublisher(Job parent, String path) {
        super(parent, path);
    }

    /**
     * Sets the value of the checkbox for "mark on unstable warning".
     *
     * @param state The boolean state of the checkbox
     */
    public void setMarkOnUnstableWarning(boolean state) {
        controlMarkOnUnstableWarning.check(state);
    }

    /**
     * Sets the value of the checkbox for "mark on build failed".
     *
     * @param state The boolean state of the checkbox
     */
    public void setMarkOnBuildFail(boolean state) {
        controlMarkOnBuildFail.check(state);
    }

    /**
     * Sets the value of the checkbox for "show graph".
     *
     * @param state The boolean state of the checkbox
     */
    public void setShowGraphs(boolean state) {
        controlShowGraphs.check(state);
    }

    /**
     * Sets the rule type and value for it.
     *
     * @param type The type of the parsing-rule (see: {@link RuleType})
     * @param rule Whole path to the rule if the type is a {@link RuleType#PROJECT}. An already configured log-parser rule if the type is a {@link RuleType#GLOBAL}.
     */
    public void setRule(RuleType type, String rule) {
        controlRuleType.choose(type.getProjecttype());
        switch (type) {
            case PROJECT:
                control("projectRulePath").set(rule);
                break;
            case GLOBAL:
                control("parsingRulesPath").select(rule);
                break;
            default:
                break;
        }
    }

    /**
     * Sets a new rule as {@link RuleType#PROJECT}.
     *
     * @param resource The {@link Resource} object of a rule file.
     */
    public void setRule(Resource resource) {
        setRule(RuleType.PROJECT, resource.url.getPath());
    }
}
