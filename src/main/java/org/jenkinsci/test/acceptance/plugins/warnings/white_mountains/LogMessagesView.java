package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

/**
 * Page object for the error and info messages view.
 * 
 * @author Alexander Praegla
 * @author Arne Schöntag
 * @author Nikolai Wohlgemuth
 */
public class LogMessagesView extends PageObject {
    private final Control errors = control(By.id("errors"));
    private final Control info = control(By.id("info"));
    
    public LogMessagesView(final Build job, final String id) {
        super(job, job.url(id + "Result/info/"));
    }

    /**
     * Creates a new info and error view.
     *
     * @param injector
     *         injector
     * @param url
     *         the URL of the view
     */
    public LogMessagesView(final Injector injector, final URL url, final String id) {
        super(injector, url);
    }

    /**
     * Returns the error messages.
     *
     * @return all error messages
     */
    public List<String> getErrorMessages() {
        return getElementsFromContainingDivs(errors);
    }

    /**
     * Returns the info messages.
     *
     * @return all info messages
     */
    public List<String> getInfoMessages() {
        return getElementsFromContainingDivs(info);
    }

    private List<String> getElementsFromContainingDivs(final Control control) {
        return control.resolve().findElements(by.xpath("div")).stream().map(WebElement::getText).collect(Collectors.toList());
    }
}
