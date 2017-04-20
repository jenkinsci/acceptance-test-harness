package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Helper class for configuring global settings of LogParser.
 *
 * Created by Daniel Sikeler.
 */
public class LogParserGlobalConfig extends PageAreaImpl {

    /**
     * Repeatable-add Button.
     */
    private final Control addButton;
    
    public LogParserGlobalConfig(JenkinsConfig context) {
        super(context, "/hudson-plugins-logparser-LogParserPublisher/log-parser");
        addButton = control("repeatable-add");
    }

    /**
     * Add a new LogParser configuration.
     * @param description The description.
     * @param path The path to the rules file.
     */
    public void addParserConfig(String description, String path) {
        String rulePath = createPageArea("rule", new Runnable() {
            @Override
            public void run() {
                addButton.click();
            }
        });
        Rule rule = new Rule(getPage(), rulePath);
        rule.description.set(description);
        rule.path.set(path);
    }

    /**
     * PageArea generated after repeatable-add is clicked.
     */
    private class Rule extends PageAreaImpl {
        public Rule(PageObject parent, String path) {
            super(parent, path);
        }

        /**
         * Description input-field.
         */
        public final Control description = control("name");
        /**
         * Path input-field.
         */
        public final Control path = control("path");
    }
}
