//
// +-----------------------------------------------------+
// |              =========================              |
// |              !  W  A  R  N  I  N  G  !              |
// |              =========================              |
// |                                                     |
// | This file is  N O T   P A R T  of the jenkins       |
// | acceptance test harness project's source code!      |
// |                                                     |
// | This file is only used for testing purposes w.r.t   |
// | the task scanner plugin test.                       |
// |                                                     |
// +-----------------------------------------------------+
//


package org.jenkinsci.test.acceptance.plugins.jira;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;

import javax.inject.Inject;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class TSRJiraGlobalConfig extends PageArea {

    @Inject
    public JiraGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-plugins-jira-JiraProjectProperty");
    }

    // TODO: make this work properly when the site exists already
    public void addSite(URL url, String user, String password) {
        control("repeatable-add").click();
        control("sites/url").set(url);
        control("sites/userName").set(user);
        control("sites/password").set(password);
    }
}
