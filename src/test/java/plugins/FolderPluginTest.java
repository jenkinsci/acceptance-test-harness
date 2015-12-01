/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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

import org.hamcrest.MatcherAssert;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractClassBasedJUnitTest;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FolderItem;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Acceptance tests for the CloudBees Folder Plugins.
 */
@WithPlugins("cloudbees-folder")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FolderPluginTest extends AbstractClassBasedJUnitTest {
    /** Test folder name. */
    private static final String F01 = "F01";
    /** Test folder name. */
    private static final String F02 = "F02";

    /** First test folder. */;
    private static FolderItem f01;

    /**
     * Checks that a folder exists and has the provided name.
     */
    private void checkFolder(FolderItem folder, String name) {
        folder.open();
        MatcherAssert.assertThat(driver, Matchers.hasContent(name));
    }
    
    /**
     * First simple test scenario: Folder creation (JENKINS-31648).
     * <ol>
     * <li>We create a folder named "F01".</li>
     * <li>We check the folder exists and we can enter it.</li>
     * </ol>
     */
    @Test
    public void test001CreateFolder() {
        final FolderItem job = jenkins.jobs.create(FolderItem.class, F01);
        job.save();
        jenkins.open();
        checkFolder(job, F01);
        f01 = job;
    }
    
    /**
     * Simple folder hierarchy test scenario: Folder creation (JENKINS-31698).
     * <ol>
     * <li>We start with the folder created in the previous test.</li>
     * <li>We check the folder exists and we can enter it.</li>
     * <li>We create a folder name "F02" inside "F01" and check it.</li>
     * <li>We visit "F01" and the root page, create a folder named "F01" inside the existing "F01" one and check it.
     * </ol>
     */
    @Test
    public void test002CreateFolderHierarchy() {
        Assert.assertNotNull(f01);
        final FolderItem child1 = f01.jobs.create(FolderItem.class, F02);
        child1.save();
        checkFolder(child1, F02);
        f01.open();
        jenkins.open();
        final FolderItem child2 = f01.jobs.create(FolderItem.class, F01);
        child2.save();
        checkFolder(child2, F01);
    }
}
