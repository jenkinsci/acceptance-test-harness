/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.plugins.audit_trail;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsLogger;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class AuditTrailLogger extends JenkinsLogger {

    private static final Pattern LOG_PATTERN = Pattern.compile("((?:\\/\\w+)+.*?) by (.*)");

    private AuditTrailLogger(Jenkins jenkins, String name) {
        super(jenkins, name);
    }

    public static AuditTrailLogger create(Jenkins jenkins) {
        if (jenkins.getPlugin("audit-trail").isNewerThan("2.0")) {
            return new ExposedFile(jenkins);
        }

        final SystemLogger logger = new SystemLogger(jenkins);

        logger.waitFor().until(new Callable<WebElement>() {
            @Override
            public WebElement call() throws Exception {
                logger.open();
                return logger.getElement(by.xpath("//h1[text()='%s']", logger.name));
            }
        });
        return logger;
    }

    public abstract List<String> getEvents();

    /**
     * Audit Trail pre 2.0 uses system logger.
     */
    private static class SystemLogger extends AuditTrailLogger {

        public SystemLogger(Jenkins jenkins) {
            super(jenkins, "Audit Trail");
        }

        @Override
        public List<String> getEvents() {
            open();
            List<String> events = new ArrayList<>();
            for (WebElement e : all(by.css("#main-panel pre"))) {
                Matcher m = LOG_PATTERN.matcher(e.getText());
                if (!m.matches()) {
                    continue; // Earlier versions used one element per log entry newer use two
                }
                events.add(m.group(1));
            }
            return events;
        }
    }

    /**
     * Expose file through /userContent/ and wrap in Logger.
     * <p>
     * Traditional logger in no longer created after Audit Trail 2.0
     */
    private static class ExposedFile extends AuditTrailLogger {

        public ExposedFile(Jenkins jenkins) {
            super(jenkins, "../userContent/audit-trail.log");

            String logfile = jenkins.runScript(
                    "def log = new File(Jenkins.instance.rootDir, 'userContent/audit-trail.log');" +
                            "log.createNewFile();" +
                            "println log.absolutePath;"
            );

            jenkins.configure();
            AuditTrailGlobalConfiguration area = new AuditTrailGlobalConfiguration(jenkins.getConfigPage());
            area.addLogger.selectDropdownMenu("Log file");

            area.control("loggers/log").set(logfile);
            area.control("loggers/limit").set(10);
            area.control("loggers/count").set(1);
            jenkins.save();
        }

        @Override
        public boolean isEmpty() {
            return getContent().isEmpty();
        }

        @Override
        public boolean hasLogged(Pattern pattern) {
            return pattern.matcher(getContent()).find();
        }

        @Override
        public List<String> getEvents() {
            try {
                List<String> events = new ArrayList<>();
                for (String line : (List<String>) IOUtils.readLines(url.openStream(), StandardCharsets.UTF_8)) {
                    Matcher m = LOG_PATTERN.matcher(line);
                    m.find();
                    events.add(m.group(1));
                }

                return events;
            }
            catch (IOException ex) {
                throw new AssertionError("Audit trail log not exposed", ex);
            }
        }

        private String getContent() {
            try {
                return IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
            }
            catch (IOException ex) {
                throw new AssertionError("Audit trail log not exposed", ex);
            }
        }
    }
}
