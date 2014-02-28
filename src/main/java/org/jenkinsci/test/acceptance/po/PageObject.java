package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import groovy.lang.Closure;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject {
    /**
     * Access to the rest of the world.
     */
    protected final Injector injector;

    @javax.inject.Inject
    protected WebDriver driver;

    public PageObject(Injector injector) {
        this.injector = injector;
        injector.injectMembers(this);
    }

    public void configure(Closure body) {
        driver.get(getConfigUrl());
        body.call(this);
        save();
    }

    protected String getConfigUrl() {
        return getUrl()+"/configure";
    }

    public void save() {
        clickButton("Save");
        if (driver.getPageSource().contains("This page expects a form submission")) {
            throw new AssertionError("Job was not saved\n"+driver.getPageSource());
        }
    }

    public String getJsonApiUrl() {
        return getUrl()+"/api/json";
    }

    public void clickButton(String text) {
        driver.findElement(By.xpath("//button[text()='"+text"']")).click();
    }
}
