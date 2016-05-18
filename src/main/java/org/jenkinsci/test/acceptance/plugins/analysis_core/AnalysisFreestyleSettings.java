package org.jenkinsci.test.acceptance.plugins.analysis_core;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.WebElement;

/**
 * Abstract job configuration class.
 * @author Fabian Trampusch
 */
public abstract class AnalysisFreestyleSettings extends AnalysisSettings {
    /**
     * The input for the file name.
     */
    public final Control pattern = control("pattern");

    /**
     * Constructor for the build settings page area.
     *
     * @param parent       the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AnalysisFreestyleSettings(Job parent, String selectorPath) {
        super(parent, selectorPath);
    }

    /**
     * Sets the pattern to the specified value and leaves the input field with tab so that the background
     * validation will be started.
     *
     * @param value the new pattern value
     * @return the validation result
     */
    public String validatePattern(final String value) {
        pattern.set(value);
        pattern.sendKeys("\t");
        elasticSleep(50); // wait for validation

        // TODO: Use pattern to find the error div rather than the publisher
        WebElement element = find(by.xpath("//div[@name='publisher']"));
        List<WebElement> errors = element.findElements(by.xpath(".//div[@class='error']"));
        if (errors.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return errors.get(0).getText();
    }
}
