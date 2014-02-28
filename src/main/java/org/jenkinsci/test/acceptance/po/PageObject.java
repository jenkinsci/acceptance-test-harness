package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.inject.Injector;
import groovy.lang.Closure;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject extends CapybaraPortingLayer {
    /**
     * Access to the rest of the world.
     */
    protected final Injector injector;

    @Inject
    protected ObjectMapper jsonParser;


    public PageObject(Injector injector) {
        this.injector = injector;
        injector.injectMembers(this);
    }

    /**
     * URL of the object that this page object represents to, relative to the context path.
     * (Thus Jenkins top page always return "/" from this method.)
     *
     * Ends without '/', such as "/job/foo"
     */
    public abstract String getUrl();

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

    /**
     * Makes the API call and obtains JSON representation.
     */
    public JsonNode getJson() throws IOException {
        return jsonParser.readTree(new URL(getJsonApiUrl()));
    }

    /**
     * Repeated evaluate the given predicate until it returns true.
     *
     * If it times out, an exception will be thrown.
     */
    public void waitForCond(Predicate<WebDriver> block, int timeoutSec) throws InterruptedException {
        long endTime = System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(timeoutSec);
        while (System.currentTimeMillis()<endTime) {
            if (block.apply(driver))
                return;
            Thread.sleep(1000);
        }
        throw new TimeoutException("Failed to wait for condition "+block);
    }
}
