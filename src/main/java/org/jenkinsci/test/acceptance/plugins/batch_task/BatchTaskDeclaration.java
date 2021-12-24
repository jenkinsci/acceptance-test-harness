package org.jenkinsci.test.acceptance.plugins.batch_task;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
public class BatchTaskDeclaration extends PageAreaImpl {
    private static final String PREFIX = "/properties/hudson-plugins-batch_task-BatchTaskProperty/on";

    private String name;
    private final Job job;

    public static BatchTaskDeclaration add(final Job job, String name) {
        String path = job.createPageArea(PREFIX + "/t", () -> {
            WebElement checkbox = job.find(by.checkbox("Batch tasks"));

            WebElement input = checkbox.findElement(by.xpath(CapybaraPortingLayerImpl.LABEL_TO_INPUT_XPATH));

            if (!input.isSelected()) {
                checkbox.click();
            } else {
                job.clickButton("Add another task...");
            }
        });

        BatchTaskDeclaration b = new BatchTaskDeclaration(job, path);
        b.setName(name);
        return b;
    }

    public BatchTaskDeclaration(Job job, String prefix) {
        super(job, prefix);
        this.job = job;
    }

    public void setName(String name) {
        this.name = name;
        control("name").set(name);
    }

    public void setScript(String script) {
        control("script").set(script);
    }

    public BatchTask getTask() {
        return new BatchTask(job, name);
    }
}
