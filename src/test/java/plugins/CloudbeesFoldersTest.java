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

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.AbstractPipelineTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.TopLevelItem;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.util.List;

import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.Matchers.hasURL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests CloudBees Folders plugin
 */
@WithPlugins("cloudbees-folder")
public class CloudbeesFoldersTest extends AbstractPipelineTest {

    private final static String FOLDER1_NAME = "folder1";
    private final static String FOLDER2_NAME = "folder2";
    private final static String FOLDER3_NAME = "folder3";

    private final static String JOB1_NAME = "job1";
    private final static String JOB2_NAME = "job2";
    private final static String JOB3_NAME = "job3";
    private final static String JOB4_NAME = "job4";

    private final static String ALL_VIEW = "All";
    private final static String MY_VIEW = "myView";

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
    public void folderViewsTest() {
        final Folder folder1 = this.createFolder(FOLDER1_NAME);
        folder1.open();

        this.checkViews(folder1, ALL_VIEW, ALL_VIEW);

        final ListView myView = folder1.getViews().create(ListView.class, MY_VIEW);
        myView.open();

        this.checkViews(folder1, MY_VIEW, ALL_VIEW, MY_VIEW);

        folder1.selectView(ListView.class, ALL_VIEW);

        this.checkViews(folder1, ALL_VIEW);

        myView.delete();

        this.checkViews(folder1, ALL_VIEW, ALL_VIEW);
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

    private Folder createFolder(final String name) {
        return jenkins.jobs.create(Folder.class, name);
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
            IOUtils.toString(f.url("").openStream());
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
                assertTrue(viewNames.contains(expectedView));
            }
        }

        final String activeView = f.getActiveViewName();
        assertEquals(expectedActiveView, activeView);
    }

}