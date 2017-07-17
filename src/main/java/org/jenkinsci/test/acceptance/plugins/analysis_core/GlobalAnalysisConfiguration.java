package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Jenkins global configuration section for the static analysis plug-ins.
 *
 * @author Ullrich Hafner
 */
public class GlobalAnalysisConfiguration extends PageAreaImpl {
    private final Control quiteMode = control("quiteMode");
    private final Control failOnCorrupt = control("failOnCorrupt");
    private final Control noAuthors = control("noAuthors");

    /**
     * Creates a new parsers configuration page area.
     *
     * @param context Jenkins global configuration page
     */
    public GlobalAnalysisConfiguration(final JenkinsConfig context) {
        super(context, "/hudson-plugins-analysis-core-GlobalSettings");
    }

    public void setNoAuthors(final boolean doNotShowAuthors) {
        noAuthors.check(doNotShowAuthors);
    }
    public void setFailBuildOnCorruptFiles(final boolean failBuildOnCorruptFiles) {
        failOnCorrupt.check(failBuildOnCorruptFiles);
    }
    public void setQuiteMode(final boolean beQuite) {
        quiteMode.check(beQuite);
    }
}
