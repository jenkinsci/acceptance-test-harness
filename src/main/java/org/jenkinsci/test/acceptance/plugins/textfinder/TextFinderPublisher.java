package org.jenkinsci.test.acceptance.plugins.textfinder;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.WebElement;

/**
 * Created by Martin Ende on 5/17/14.
 */

@Describable("Jenkins Text Finder")
public class TextFinderPublisher extends PostBuildStep {

    public final WebElement filePath = find(by.xpath("//input[@name='_.fileSet']"));
    public final WebElement regEx =find(by.xpath("//input[@name='_.regexp']"));
    public final WebElement succeedIfFound = find(by.xpath("//input[@name='_.succeedIfFound']"));
    public final WebElement unstableIfFound = find(by.xpath("//input[@name='_.unstableIfFound']"));



    public TextFinderPublisher(Job parent, String path) {
        super(parent, path);
    }

}
