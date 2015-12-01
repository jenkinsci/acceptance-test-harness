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
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.jenkinsci.test.acceptance.po.PageObject.createRandomName;

/**
 * Acceptance tests for the CloudBees Folder Plugins.
 */
@WithPlugins("cloudbees-folder")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FolderPluginTest extends AbstractClassBasedJUnitTest {
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
     * <li>We create a folder.</li>
     * <li>We check the folder exists and we can enter it.</li>
     * </ol>
     */
    @Test
    public void test001CreateFolder() {
        final String name = createRandomName();
        final FolderItem job = jenkins.jobs.create(FolderItem.class, name);
        job.save();
        jenkins.open();
        checkFolder(job, name);
    }
    
    /**
     * Simple folder hierarchy test scenario: Folder creation (JENKINS-31698).
     * <ol>
     * <li>We create a first level folder (we'll call it F01 in the description).</li>
     * <li>We check the folder exists and we can enter it.</li>
     * <li>We create a folder name with a different name inside F01 step and check it.</li>
     * <li>We visit F01 and the root page, create a folder named with the same name as F01 inside it and check it.
     * </ol>
     */
    @Test
    public void test002CreateFolderHierarchy() {
        final String parentName = createRandomName();
        final FolderItem parent = jenkins.jobs.create(FolderItem.class, parentName);
        parent.save();
        final String childName = createRandomName();
        final FolderItem child1 = parent.jobs.create(FolderItem.class, childName);
        child1.save();
        checkFolder(child1, childName);
        parent.open();
        jenkins.open();
        final FolderItem child2 = parent.jobs.create(FolderItem.class, parentName);
        child2.save();
        checkFolder(child2, parentName);
    }
}
