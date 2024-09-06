package org.jenkinsci.test.acceptance.plugins.textfinder;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.WebElement;

/**
 * This class provides the ability to add a Jenkins Text Finder post build step
 * to the job.
 * <p>
 * It provides access to the particular web elements to configure the post build step.
 * <p>
 * This post build step requires installation of the text-finder plugin.
 *
 * @author Martin Ende
 */
@Describable("Jenkins Text Finder")
public class TextFinderPublisher extends AbstractStep implements PostBuildStep {

    public final WebElement filePath = find(by.xpath("//input[@name='_.fileSet']"));
    public final WebElement regEx = find(by.xpath("//input[@name='_.regexp']"));
    public final WebElement succeedIfFound = find(by.xpath("//input[@name='_.succeedIfFound']"));
    public final WebElement unstableIfFound = find(by.xpath("//input[@name='_.unstableIfFound']"));

    public TextFinderPublisher(Job parent, String path) {
        super(parent, path);
    }
}
