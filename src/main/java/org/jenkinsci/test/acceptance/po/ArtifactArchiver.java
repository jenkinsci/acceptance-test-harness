package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.ByFactory;
import org.openqa.selenium.By;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Archive the artifacts")
public class ArtifactArchiver extends AbstractStep implements PostBuildStep {

    public ArtifactArchiver(Job parent, String path) {
        super(parent, path);
    }

    public ArtifactArchiver includes(String value) {
        control("artifacts").set(value);
        return this;
    }

    public ArtifactArchiver excludes(String value) {
        // TODO fix form-element-path plugin for new button markup
        find(new ByFactory().path(getPath())).findElement(By.cssSelector(".advanced-button")).click();
        control("excludes").set(value);
        return this;
    }
}
