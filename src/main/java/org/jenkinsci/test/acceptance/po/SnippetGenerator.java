package org.jenkinsci.test.acceptance.po;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * {@link PageObject} for the snippet generator to create bits of code for individual steps.
 */
public class SnippetGenerator extends PageObject {
    private static final String URI = "pipeline-syntax/";

    /**
     * Creates a new page object for the snippet generator.
     *
     * @param context
     *         job context
     */
    public SnippetGenerator(final WorkflowJob context) {
        super(context,  context.url(URI));
    }

    @Override
    protected WorkflowJob getContext() {
        return (WorkflowJob) super.getContext();
    }

    /**
     * Generates the sample pipeline script.
     *
     * @return the generated script
     */
    public String generateScript() {
        WebElement button = find(By.id("generatePipelineScript-button"));
        button.click();
        elasticSleep(500);
        WebElement textarea = find(By.id("prototypeText"));
        return StringUtils.defaultString(textarea.getAttribute("value"));
    }
}
