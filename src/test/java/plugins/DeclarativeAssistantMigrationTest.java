/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.jenkinsci.test.acceptance.Matchers.hasElement;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;
import org.openqa.selenium.By;

@WithPlugins({"declarative-pipeline-migration-assistant","declarative-pipeline-migration-assistant-api"})
public class DeclarativeAssistantMigrationTest
    extends AbstractJUnitTest
{

    @Test
    public void basicDeclarativeTests() throws Exception {
        FreeStyleJob j = jenkins.jobs.create( FreeStyleJob.class, "simple-job-to-declarative");
        j.configure();
        ShellBuildStep shell = j.addBuildStep( ShellBuildStep.class);
        shell.command("echo 1");

        j.apply();
        j.save();

        clickLink("To Declarative");
        assertThat(driver, hasElement( By.className( "rectangle-conversion-success")));
        assertThat(driver, hasElement(By.className("review-converted")));
        assertThat(driver, hasElement(By.id("jenkinsfile-content")));
        String jenkinsFile =  driver.findElement(By.id("jenkinsfile-content")).getText();
        assertThat(jenkinsFile, containsString( "echo 1" ));
    }
}
