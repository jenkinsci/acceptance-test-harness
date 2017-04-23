package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;

/**
 * @author Kohsuke Kawaguchi
 */
public class ViewSteps extends AbstractSteps {
    @And("^I build \"([^\"]*)\" in view$")
    public void I_build_in_view(String job) throws Throwable {
        if (my.view==null)
            jenkins.open();
        else
            my.view.open();

        find(by.xpath("//a[contains(@href, '/%1$s/build?')]/img[contains(@title, 'Schedule a')]", job)).click();
    }
}
