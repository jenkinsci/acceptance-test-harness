/*
 * The MIT License
 *
 * Copyright 2015-2023 CloudBees, Inc.
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

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.Matchers.hasURL;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectDoesNotExist;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Acceptance tests for the CloudBees Folder Plugins.
 */
@WithPlugins("cloudbees-folder")
public class FolderPluginTest extends AbstractJUnitTest {
    /** Test folder name. */
    private static final String F01 = "F01";
    /** Test folder name. */
    private static final String F02 = "F02";
    /** Test view names. */
    private static final String ALL_VIEW = "All";
    private static final String MY_VIEW = "MyView";
    /** Test job names. */
    private static final String JOB1 = "job1";
    private static final String JOB2 = "job2";
    /** Credential data. */
    private final static String CRED_ID = "mycreds";
    private final static String CRED_USER = "myUser";
    private final static String CRED_PASS = "myPass";
    private final static String CRED_INJECTED_MESSAGE = "credentials have been injected";
    
    /**
     * Checks that a folder exists and has the provided name.
     */
    private void checkFolder(Folder folder, String name) {
        folder.open();
        assertThat(driver, Matchers.hasContent(name));
    }

    private Folder createFolder(final String name) {
        return jenkins.jobs.create(Folder.class, name);
    }
    
    /**
     * First simple test scenario: Folder creation (JENKINS-31648).
     * <ol>
     * <li>We create a folder named "F01".</li>
     * <li>We check the folder exists and we can enter it.</li>
     * </ol>
     */
    @Test
    public void createFolder() {
        final Folder folder = jenkins.jobs.create(Folder.class, F01);
        folder.save();
        jenkins.open();
        checkFolder(folder, F01);
    }
    
    /**
     * Simple folder hierarchy test scenario: Folder creation (JENKINS-31648).
     * <ol>
     * <li>We create a folder named "F01".</li>
     * <li>We check the folder exists and we can enter it.</li>
     * <li>We create a folder name "F02" inside "F01" and check it.</li>
     * <li>We visit "F01" and the root page, create a folder named "F01" inside the existing "F01" one and check it.
     * </ol>
     */
    @Test
    public void createFolderHierarchy() {
        final Folder parent = jenkins.jobs.create(Folder.class, F01);
        parent.save();
        checkFolder(parent, F01);
        final Folder child1 = parent.getJobs().create(Folder.class, F02);
        child1.save();
        checkFolder(child1, F02);
        parent.open();
        jenkins.open();
        final Folder child2 = parent.getJobs().create(Folder.class, F01);
        child2.save();
        checkFolder(child2, F01);
    }

    /**
     * Simple test scenario to validate views in a folder are working properly.
     */
    @Test
    public void folderViewsTest() {
        final Folder folder = jenkins.jobs.create(Folder.class, F01);
        folder.open();

        this.checkViews(folder, ALL_VIEW, ALL_VIEW);

        ListView myView = folder.getViews().create(ListView.class, MY_VIEW);
        myView.open();

        this.checkViews(folder, MY_VIEW, ALL_VIEW, MY_VIEW);

        folder.selectView(ListView.class, ALL_VIEW);

        this.checkViews(folder, ALL_VIEW);

        myView = folder.selectView(ListView.class, MY_VIEW);

        this.checkViews(folder, MY_VIEW);

        myView.delete();

        this.checkViews(folder, ALL_VIEW, ALL_VIEW);
    }

    /**
     * Test scenario to validate credentials scoped in folders.
     */
    @Test
    @WithPlugins({ "credentials", "credentials-binding", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step" })
    public void folderScopedCredentialsTest() {
        final Folder folder = jenkins.jobs.create(Folder.class, F01);
        final FreeStyleJob job1 = folder.getJobs().create(FreeStyleJob.class, JOB1);

        waitFor(folder, pageObjectExists(), 1000);
        waitFor(job1, pageObjectExists(), 1000);

        final WorkflowJob job2 = createWorkflowJob(folder);
        waitFor(job2, pageObjectExists(), 1000);

        final String console = job2.startBuild().shouldSucceed().getConsole();
        assertThat(console, containsRegexp(CRED_INJECTED_MESSAGE));

        job1.delete();
        waitFor(job1, pageObjectDoesNotExist(), 1000);

        job2.delete();
        waitFor(job2, pageObjectDoesNotExist(), 1000);

        folder.delete();
        waitFor(folder, pageObjectDoesNotExist(), 1000);
    }

    private final static String FOLDER1_NAME = "folder1";
    private final static String FOLDER2_NAME = "folder2";
    private final static String FOLDER3_NAME = "folder3";

    private final static String JOB1_NAME = "job1";
    private final static String JOB2_NAME = "job2";
    private final static String JOB3_NAME = "job3";
    private final static String JOB4_NAME = "job4";

    @Test
    public void basicOperationsTest()  {
        final Folder originalFolder = this.createFolder(FOLDER1_NAME);

        this.checkItemExists(originalFolder, true);
        this.checkItemNameAndUrl(originalFolder, FOLDER1_NAME);

        final Folder renamedFolder = originalFolder.renameTo(FOLDER2_NAME);

        this.checkItemExists(originalFolder, false);
        this.checkItemExists(renamedFolder, true);
        this.checkItemNameAndUrl(renamedFolder, FOLDER2_NAME);

        renamedFolder.delete();

        this.checkItemExists(renamedFolder, false);
    }

    @Test
    @WithPlugins("workflow-job")
    public void nestedOperationsTest()  {
        final Folder folder1 = this.createFolder(FOLDER1_NAME);
        final FreeStyleJob job1 = folder1.getJobs().create(FreeStyleJob.class, JOB1_NAME);

        this.checkItemExists(job1, true);
        this.checkItemNameAndUrl(job1, JOB1_NAME, folder1);

        final Folder folder2 = folder1.getJobs().create(Folder.class, FOLDER2_NAME);

        this.checkItemExists(folder2, true);
        this.checkItemNameAndUrl(folder2, FOLDER2_NAME);

        final FreeStyleJob job2 = folder2.getJobs().create(FreeStyleJob.class, JOB2_NAME);

        this.checkItemExists(job2, true);
        this.checkItemNameAndUrl(job2, JOB2_NAME, folder1, folder2);

        final WorkflowJob job3 = folder2.getJobs().create(WorkflowJob.class, JOB3_NAME);

        this.checkItemExists(job3, true);
        this.checkItemNameAndUrl(job3, JOB3_NAME, folder1, folder2);

        final FreeStyleJob job2Renamed = job2.renameTo(JOB4_NAME);

        this.checkItemExists(job2, false);
        this.checkItemExists(job2Renamed, true);
        this.checkItemNameAndUrl(job2Renamed, JOB4_NAME, folder1, folder2);

        job3.delete();

        waitFor(driver, hasURL(folder2.url), 3);

        this.checkItemExists(job3, false);

        folder1.delete();

        waitFor(driver, hasURL(jenkins.url), 3);
        this.checkItemExists(folder1, false);
        this.checkItemExists(job3, false);
        this.checkItemExists(folder2, false);
        this.checkItemExists(job2Renamed, false);
    }

    @Test
    public void copyTest() {
        final Folder folder1 = this.createFolder(FOLDER1_NAME);
        final FreeStyleJob job1 = folder1.getJobs().create(FreeStyleJob.class, JOB1_NAME);
        final Folder folder2 = this.createFolder(FOLDER2_NAME);
        final Folder folder3 = this.createFolder(FOLDER3_NAME);

        folder2.getJobs().copy(folder1.name + "/" + job1.name, job1.name);

        final FreeStyleJob job2 = folder2.getJobs().get(FreeStyleJob.class, job1.name);
        this.checkItemExists(job2, true);

        folder3.getJobs().copy(folder2.name, folder2.name);

        final Folder f3f2 = folder3.getJobs().get(Folder.class, folder2.name);
        final FreeStyleJob job3 = f3f2.getJobs().get(FreeStyleJob.class, job1.name);
        this.checkItemExists(job2, true);
        this.checkItemExists(f3f2, true);
        this.checkItemExists(job3, true);

        try {
            job3.startBuild();
            fail("Copied job should not be able to build until saved");
        } catch (final NoSuchElementException ex) {
            assertThat(ex.getMessage(), containsRegexp("Build Now"));
            assertThat(ex.getMessage(), containsRegexp("Unable to locate"));
        }

        job3.configure();
        job3.save();
        job3.startBuild().shouldSucceed();

        f3f2.getJobs().copy("/" + folder1.name + "/" + job1.name, job1.name);

        assertThat(driver, hasContent("A job already exists with the name ‘" +  job1.name + "’"));
    }

    private void checkItemNameAndUrl(final TopLevelItem item, final String itemName, final TopLevelItem... parentItems) {
        item.open();
        assertThat("Item name is not displayed", driver, hasContent(itemName));

        if (parentItems.length > 0) {
            String itemPath = "";

            for (final TopLevelItem parentItem : parentItems) {
                itemPath += String.format("%s/", parentItem.name);
            }

            itemPath += item.name;

            assertThat("Complete path for item is not displayed", driver, hasContent(itemPath));
        }
    }

    private void checkItemExists(final TopLevelItem f, final boolean expectedExists) {
        boolean exists = true;

        try {
            IOUtils.toString(f.url("").openStream(), StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            exists = false;
        }

        assertEquals(exists, expectedExists);
    }

    private void checkViews(final Folder f, final String expectedActiveView, final String... expectedExistingViews) {
        if (expectedExistingViews.length > 0) {
            final List<String> viewNames = f.getViewsNames();
            assertEquals(expectedExistingViews.length, viewNames.size());

            for (final String expectedView : expectedExistingViews) {
                assertTrue(viewNames.stream().anyMatch(expectedView::equalsIgnoreCase));
            }
        }

        final String activeView = f.getActiveViewName();
        assertThat(activeView, equalToIgnoringCase(expectedActiveView));
    }

    private void createCredentials(final Folder f) {
        final CredentialsPage mc = new CredentialsPage(f, ManagedCredentials.DEFAULT_DOMAIN);
        mc.open();

        final UserPwdCredential cred = mc.add(UserPwdCredential.class);
        cred.username.set(CRED_USER);
        cred.password.set(CRED_PASS);
        cred.setId(CRED_ID);

        mc.create();
    }

    private WorkflowJob createWorkflowJob(final Folder f) {
        this.createCredentials(f);

        final String script = String.format("node {\n" +
                "    withCredentials([usernamePassword(credentialsId: '%s', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {\n" +
                "        sh '[ \"$USERNAME\" = \"%s\" ] && [ \"$PASSWORD\" = \"%s\" ] && echo \"%s\"'\n" +
                "    }\n" +
                "}",
                CRED_ID, CRED_USER, CRED_PASS, CRED_INJECTED_MESSAGE);

        final WorkflowJob job = f.getJobs().create(WorkflowJob.class, JOB2);
        job.script.set(script);
        job.save();

        return job;
    }
}
