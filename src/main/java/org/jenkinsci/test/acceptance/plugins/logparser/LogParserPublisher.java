package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.po.*;

/**
 *
 *
 * @author Michael Engel
 */
@Describable("Console output (build log) parsing")
public class LogParserPublisher extends AbstractStep implements PostBuildStep {

    // The available types of rules of the log-parser
    public enum RuleType{
        PROJECT("true"), GLOBAL("false");
        private final String projecttype;
        RuleType(String projecttype){
            this.projecttype = projecttype;
        };
        public String getProjecttype(){
            return projecttype;
        }
    }

    // available controls of the log-parser post-build-step
    private final Control controlMarkOnUnstableWarning;
    private final Control controlMarkOnBuildFail;
    private final Control controlShowGraphs;
    private final Control controlRuleType;

    /**
     * Constructor.
     * Should only be instantiated by addPublisher(...) of a job
     *
     * @param parent
     * @param path
     */
    public LogParserPublisher(Job parent, String path) {
        super(parent, path);

        controlMarkOnUnstableWarning = control("unstableOnWarning");
        controlMarkOnBuildFail = control("failBuildOnError");
        controlShowGraphs = control("showGraphs");
        controlRuleType = control("useProjectRule");
    }

    /**
     * Sets the value of the checkbox for "mark on unstable warning".
     *
     * @param state The boolean state of the checkbox
     */
    public void setMarkOnUnstableWarning(boolean state){
        controlMarkOnUnstableWarning.check(state);
    }

    /**
     * Sets the value of the checkbox for "mark on build failed".
     *
     * @param state The boolean state of the checkbox
     */
    public void setMarkOnBuildFail(boolean state){
        controlMarkOnBuildFail.check(state);
    }

    /**
     * Sets the value of the checkbox for "show graph".
     *
     * @param state The boolean state of the checkbox
     */
    public void setShowGraphs(boolean state){
        controlShowGraphs.check(state);
    }

    /**
     * Sets the rule type and value for it.
     *
     * @param type The type of the parsing-rule (see: {@link RuleType})
     * @param rule Whole path to the rule if the type is a {@link RuleType#PROJECT}. An already configured log-parser rule if the type is a {@link RuleType#GLOBAL}.
     */
    public void setRule(RuleType type, String rule){
        controlRuleType.choose(type.getProjecttype());
        switch (type){
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
}
