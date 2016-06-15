package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Managed Credentials page for credentials plugin >=2.0.7
 *
 * Created by raul on 14/06/16.
 */
public class ManagedCredentials2 extends ContainerPageObject {
    public final Control addButton = new Control(injector,by.xpath("//*[@id=\"main-panel\"]/form/table/tbody/tr[1]/td[3]/select"));
    public final Control addDomainButton = control("/repeatable-add");

    public ManagedCredentials2(Jenkins j) {
        super(j, j.url("credentials/store/system/domain/_/"));


    }

    /**
     * Adds a new credential and bind it to the page object.
     */
    public <T extends Credential> T add(String caption, Class<T> type) {
        WebElement addCredentialsLink=find(by.xpath("//*[@id=\"main-panel\"]/table[1]/tbody/tr[2]/td/a"));
        addCredentialsLink.click();
        elasticSleep(1000);
        addButton.select(caption);


        return newInstance(type, this, "/credentials");
    }

    @Override
    public void save() {
        clickButton("OK");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }
}

