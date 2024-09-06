package org.jenkinsci.test.acceptance.plugins.audit_trail;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Global configuration section of the audit-trail plugin.
 *
 * @author Kohsuke Kawaguchi
 */
public class AuditTrailGlobalConfiguration extends PageAreaImpl {

    public final Control addLogger = control("hetero-list-add[loggers]");

    public AuditTrailGlobalConfiguration(JenkinsConfig context) {
        super(context, "/hudson-plugins-audit_trail-AuditTrailPlugin");
    }

    // TODO
    //    public LoggerPageObject addLogger() {
    //        addLogger.selectDropdownMenu("Log file");
    //        ....
    //    }
}
