package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildHistory extends PageObject {
    public BuildHistory(Node parent) {
        super(parent.injector, parent.url("builds"));
    }

    public void shouldInclude(String jobName){
        assertThat(includes(jobName), is(true));
    }

    /**
     * get through all of the entries of a certain node's build history and return whether
     * a certain job was included at least once
     */
    public boolean includes(String jobName) {
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

    /**
     * Go through all of the entries of a certain node's build history and return
     * the number of times a certain job was included.
     */
    public int numberOfInclusions(String jobName) {
        int n = 0;

        open();
        for (WebElement e : all(by.xpath("//a[@href]"))) {
            if (e.getAttribute("href").endsWith("job/" + jobName + "/")) {
                n++;
            }
        }
        return n;
    }
}
