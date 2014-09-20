package org.jenkinsci.test.acceptance.plugins.batch_task;

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

    public static BatchTaskDeclaration add(Job job, String name) {
        WebElement checkbox = job.find(by.path(PREFIX));
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
        else {
            job.clickButton("Add another task...");
            job.elasticSleep(1000);
        }

        String p = job.last(by.input("batch-task.name")).getAttribute("path");
        String path_prefix = p.substring(0, p.length() - 5); // trim off '/name'

        BatchTaskDeclaration b = new BatchTaskDeclaration(job, path_prefix);
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
