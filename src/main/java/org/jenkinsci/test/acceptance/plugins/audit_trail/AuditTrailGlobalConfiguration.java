package org.jenkinsci.test.acceptance.plugins.audit_trail;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.GlobalPluginConfiguration;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;

/**
 * Global configuration section of the audit-trail plugin.
 *
 * @author Kohsuke Kawaguchi
 */
public class AuditTrailGlobalConfiguration extends GlobalPluginConfiguration {

    public final Control addLogger = control("hetero-list-add[loggers]");

    public AuditTrailGlobalConfiguration(JenkinsConfig context) {
        super(context, "audit-trail");
    }

    // TODO
//    public LoggerPageObject addLogger() {
//        addLogger.selectDropdownMenu("Log file");
//        ....
//    }
}
