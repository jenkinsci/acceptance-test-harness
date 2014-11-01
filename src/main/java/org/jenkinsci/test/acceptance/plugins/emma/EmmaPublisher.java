/*
 * The MIT License
 *
 * Copyright (c) 2014 Sony Mobile Communications Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.plugins.emma;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.WebElement;

/**
 * This class provides the ability to add the 'Record Emma Coverage Report' post build step
 * to the job.
 *
 * It provides access to the particular web elements to configure the post build step.
 *
 * This post build step requires installation of the emma plugin.
 *
 * @author Orjan Percy
 */
@Describable("Record Emma coverage report")
public class EmmaPublisher extends AbstractStep implements PostBuildStep {

    public final WebElement includes = find(by.xpath("//input[@name='emma.includes']"));
    public final WebElement maxClass = find(by.xpath("//input[@name='emmaHealthReports.maxClass']"));
    public final WebElement maxMethod = find(by.xpath("//input[@name='emmaHealthReports.maxMethod']"));
    public final WebElement maxBlock = find(by.xpath("//input[@name='emmaHealthReports.maxBlock']"));
    public final WebElement maxLine = find(by.xpath("//input[@name='emmaHealthReports.maxLine']"));
    public final WebElement maxCondition = find(by.xpath("//input[@name='emmaHealthReports.maxCondition']"));
    public final WebElement minClass = find(by.xpath("//input[@name='emmaHealthReports.minClass']"));
    public final WebElement minMethod = find(by.xpath("//input[@name='emmaHealthReports.minMethod']"));
    public final WebElement minBlock = find(by.xpath("//input[@name='emmaHealthReports.minBlock']"));
    public final WebElement minLine = find(by.xpath("//input[@name='emmaHealthReports.minLine']"));
    public final WebElement minCondition = find(by.xpath("//input[@name='emmaHealthReports.minCondition']"));

    public EmmaPublisher(Job parent, String path) {
        super(parent, path);
    }

    /*
     * Configure health reporting thresholds.
     */
    public void setReportingThresholds(int maxClass, int maxMethod, int maxBlock, int maxLine, int maxCondition,
                                       int minClass, int minMethod, int minBlock, int minLine, int minCondition) {
        this.maxClass.sendKeys(Integer.toString(maxClass));
        this.maxMethod.sendKeys(Integer.toString(maxMethod));
        this.maxBlock.sendKeys(Integer.toString(maxBlock));
        this.maxLine.sendKeys(Integer.toString(maxLine));
        this.maxCondition.sendKeys(Integer.toString(maxCondition));
        this.minClass.sendKeys(Integer.toString(minClass));
        this.minMethod.sendKeys(Integer.toString(minMethod));
        this.minBlock.sendKeys(Integer.toString(minBlock));
        this.minLine.sendKeys(Integer.toString(minLine));
        this.minCondition.sendKeys(Integer.toString(minCondition));
    }
}

