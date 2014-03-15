package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 Feature: Add spport for dashboards
   In order to be able to create custom dashboards
   As a Jenkins user
   I want to install and configure dashboard-view plugin
 */
@WithPlugins("dashboard-view")
public class DashboardViewPluginTest extends AbstractJUnitTest {
    /**
     Scenario: Configure dashboard
       Given I have installed the "dashboard-view" plugin
       When I create a view with a type "Dashboard" and name "dashboard"
       And I configure dummy dashboard
       And I create job "job_in_view" in the view
       And I build "job_in_view" in view
       Then the build should succeed
       And the dashboard sould contain details of "job_in_view"
     */
    @Test
    public void configure_dashboard() {
        DashboardView v = jenkins.createView(DashboardView.class,"dashboard");
        v.configure();
        {
            //TODO: move this XPath to DashboardView
            find(by.path("/hetero-list-add[topPortlet]")).click();
            clickLink("Build statistics");

            find(by.path("/hetero-list-add[bottomPortlet]")).click();
            clickLink("Jenkins jobs list");
        }
        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class, "job_in_view");

        v.open();
        find(by.xpath("//a[contains(@href, '/%s/build?')]/img[contains(@title, 'Schedule a build')]",j.name)).click();
        j.getLastBuild().shouldSucceed();


    }
}
