package org.jenkinsci.test.acceptance.po;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

/**
 * @author Alexander Praegla, Nikolai Wohlgemuth, Arne Schöntag
 */
public class MessageBox extends PageObject {
    //TODO: see https://issues.jenkins-ci.org/browse/JENKINS-52106
    public static final String INFO_PANEL_XPATH = "//*[@id=\"main-panel\"]/div/div[2]/div/div/div/pre/samp";
    public static final String ERROR_PANEL_XPATH = "//*[@id=\"main-panel\"]/div/div[1]/div/div/div/pre/samp";

    public MessageBox(Job job) {super(job, job.url("lastBuild/checkstyleResult/info/"));}

    public List<String> getErrorMsgContent(){
        WebElement content = driver.findElement(by.xpath(ERROR_PANEL_XPATH));
        List result = new ArrayList();
        content.findElements(by.xpath("div")).forEach(webElement -> result.add(webElement.getText()));
        return result;
    }

    public List<String> getInfoMsgContent(){
        WebElement content = driver.findElement(by.xpath(INFO_PANEL_XPATH));
        List result = new ArrayList();
        content.findElements(by.xpath("div")).forEach(webElement -> result.add(webElement.getText()));
        return result;
    }
}
