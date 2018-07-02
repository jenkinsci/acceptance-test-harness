package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.net.URL;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;

import com.google.inject.Injector;

/**
 * Class representing the source code page. todo replace by the one written by the other team
 *
 * @author Stephan Plöderl
 */
public class SourceCodeView extends PageObject {

    public SourceCodeView(final Injector injector, final URL url) {
        super(injector, url);
    }

    /**
     * Returns the file name displayed in the header.
     *
     * @return the file name
     */
    public String getFileName() {
        String[] headerWords = find(By.tagName("h1")).getText().trim().split(" ");
        return headerWords[headerWords.length - 1];
    }
}
