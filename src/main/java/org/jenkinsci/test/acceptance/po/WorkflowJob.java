/*
 * The MIT License
 *
 * Copyright 2014 Jesse Glick.
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

package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import java.net.URL;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.Select;

@Describable("org.jenkinsci.plugins.workflow.job.WorkflowJob")
public class WorkflowJob extends Job {

    public WorkflowJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public final Control script = new Control(this, "/definition/script") {

        @Override
        public void set(String text) {
            // Whilst we can interact with the ace editor we may need to clear all the contents which is not possible.
            // The only way to clear the current text on the editor is to select all the text and delete it.
            // We can not do in a cross platform way because mac doesn't use <ctrl>+a for "select all" shortcut
            WebElement aceEditorHolder = resolve();

            // scroll into view https://github.com/SeleniumHQ/selenium/issues/17141#issuecomment-3969129937
            if (aceEditorHolder instanceof Locatable l) {
                // force a scroll into view
                l.getCoordinates().inViewPort();
            }

            // The following can set the text without javascript but this only works the first time
            // subsequent calls to set will append onto what was already set.
            /*
            WebElement we = driver.switchTo().activeElement();
            // the following will clear the contents but does not work on mac
            // new Actions(driver).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE).perform();
            we.sendKeys(text);
            */
            executeScript("""
                    if (!arguments[0].aceEditor) {
                        throw '**** Selected ACE Editor target object is not an active ACE Editor';
                    }
                    arguments[0].aceEditor.setValue(arguments[1]);
                    """, aceEditorHolder, text);
        }

        @Override
        public WebElement resolve() {
            // When the ACE editor loads it sets the textarea's style to display: none
            // and the script runs after the page loads so we need to wait for it to add all the parts needed.

            // super.resolve calls CapybaraPortingLayerImpl.find, which calls isDisplayed and fails.
            // do find directly and wait for javascript to have created all the parts.
            return waitFor(driver)
                    .ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
                    .until(d -> driver.findElement(new ByChained(
                            by.path("/definition/script"), // the form textarea will be updated to be hidden by the
                            // editor script
                            By.xpath(".."), // parent (/div[class=workflow-editor-wrapper])
                            By.className("ace_editor") // editor component injected by the editor script
                            )));
        }
    };

    public final Control sandbox = control("/definition/sandbox");

    public String copyResourceStep(String filePath) {
        final Resource res = resource(filePath);
        return String.format("sh '''%s'''%n", copyResourceShell(res, res.getName()));
    }

    @Override
    public void delete() {
        open();
        runThenHandleDialog(() -> clickLink("Delete Pipeline"));
    }

    /**
     * Selects the location of the Jenkinsfile to be a Git repository with the specified URL. The provided credentials
     * key is used to connect to the Git repository.
     *
     * @param gitRepositoryUrl the URL to the Git repository that contains the Jenkinsfile
     * @param credentialsKey   the key of the credentials to be used to connect to the repository
     */
    // TODO: provide a generic way of setting the source of the repository
    public void setJenkinsFileRepository(final String gitRepositoryUrl, final String credentialsKey) {
        select("Pipeline script from SCM");
        select("Git");
        WebElement gitUrl = waitFor(by.path("/definition/scm/userRemoteConfigs/url"), 10);
        gitUrl.sendKeys(gitRepositoryUrl);
        Select credentials =
                new Select(control(By.className("credentials-select")).resolve());
        credentials.selectByVisibleText(credentialsKey);
    }

    private void select(final String option) {
        find(by.option(option)).click();
    }
}
