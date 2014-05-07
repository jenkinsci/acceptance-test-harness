package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;

import java.util.NoSuchElementException;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildHistory extends PageObject {
    public BuildHistory(Node parent) {
        super(parent.injector, parent.url("builds"));
    }

    public void shouldInclude(String jobName) {
        open();
        for (WebElement e : all(by.xpath("//a[@href]"))) {
            if (e.getAttribute("href").endsWith("job/"+jobName+"/"))
                return;
        }
        throw new NoSuchElementException();
    }
}
