package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Helper class for configuring global settings of LogParser.
 *
 * Created by Daniel Sikeler.
 */
public class LogParserProjectConfig extends PageAreaImpl {
    
    public LogParserProjectConfig(JenkinsConfig context) {
        super(context, "/hudson-plugins-logparser-LogParserPublisher/log-parser");
    }

    /**
     * Add a new LogParser configuration.
     * @param description The description.
     * @param path The path to the rules file.
     */
    public void addParserConfig(String description, String path) {
        control("repeatable-add").click();
        control("rule/name").set(description);
        control("rule/path").set(path);
    }
}
