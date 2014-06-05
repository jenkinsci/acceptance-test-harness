package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;

import java.net.URL;

/**
 * @author Matthias Karl
 */
public class Changes extends PageObject {

    protected Changes(PageObject context, URL url) {
        super(context, url);
    }


    public boolean hasChanges() {
        try {
            //TODO: improve test to be more failproove
            find(by.xpath("//h2[text()='%s']/following-sibling::ol/li", "Summary"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }


}
