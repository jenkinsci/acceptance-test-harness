package org.jenkinsci.test.acceptance.po;

import java.net.URL;
import org.openqa.selenium.NoSuchElementException;

/**
 * @author Matthias Karl
 */
public class Changes extends PageObject {

    protected Changes(PageObject context, URL url) {
        super(context, url);
    }

    /**
     * Are there any changes in the current build.
     *
     * @return true if the build has changes.
     */
    public boolean hasChanges() {
        try {
            // TODO: improve test to be more failproove
            find(by.xpath("//h2[text()='%s']/following-sibling::ol/li", "Summary"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Is there a (diff) link for a specific file.
     * Links are present if there are changes in a file and a repository browser is specified.
     *
     * @param file name of the file with changes.
     * @return true if (diff) link for file is present.
     */
    public boolean hasDiffFileLink(String file) {
        try {
            // TODO: improve test to be more failproove
            find(by.xpath("//a[text()='/%s']/following-sibling::a[text()='(diff)']", file));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
