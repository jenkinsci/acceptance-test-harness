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

    public boolean shouldInclude(String jobName) {
        boolean isIncluded = false;

        open();
        for (WebElement e : all(by.xpath("//a[@href]"))) {
            if (e.getAttribute("href").endsWith("job/"+jobName+"/")){
                isIncluded = true;
                break;
            }
        }
        return isIncluded;
    }
}
