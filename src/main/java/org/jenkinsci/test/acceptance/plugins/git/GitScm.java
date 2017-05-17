/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.plugins.git;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;

@Describable("Git")
public class GitScm extends Scm {
    private final Control branch = control("branches/name");
    private final Control url = control("userRemoteConfigs/url");
    private final Control tool = control("gitTool");

    public GitScm(Job job, String path) {
        super(job, path);
    }

    public GitScm url(String url) {
        this.url.set(url);
        return this;
    }

    public GitScm credentials(String name) {
        Select select = new Select(control(By.className("credentials-select")).resolve());
        select.selectByVisibleText(name);
        return this;
    }

    public GitScm tool(String tool) {
        this.tool.select(tool);
        return this;
    }

    public GitScm branch(String branch) {
        this.branch.set(branch);
        return this;
    }

    public GitScm localBranch(String branch) {
        try {
            advanced();
            control("localBranch").set(branch);
        }
        catch (NoSuchElementException ex) { // Git 2.0
            addBehaviour(CheckoutToLocalBranch.class).name.set(branch);
        }
        return this;
    }

    public GitScm localDir(String dir) {
        try {
            advanced();
            control("relativeTargetDir").set(dir);
        }
        catch (NoSuchElementException ex) { // Git 2.0
            addBehaviour(CheckoutToLocalDir.class).name.set(dir);
        }
        return this;
    }

    public void enableRecursiveSubmoduleProcessing() {
        addBehaviour(RecursiveSubmodules.class).enable.click();
    }

    public GitScm remoteName(String name) {
        remoteAdvanced();
        control("userRemoteConfigs/name").set(name);
        return this;
    }

    public <T extends Behaviour> T addBehaviour(Class<T> type) {
        control("hetero-list-add[extensions]").click();
        return newInstance(type, this, "extensions");   // FIXME: find the last extension added
    }

    private void advanced() {
        control("advanced-button").click();
    }

    private void remoteAdvanced() {
        control("userRemoteConfigs/advanced-button").click();
    }

    ////////////////
    // BEHAVIOURS //
    ////////////////

    public static class Behaviour extends PageAreaImpl {
        public Behaviour(GitScm git, String path) {
            super(git, path);
        }
    }

    public static class CheckoutToLocalBranch extends Behaviour {
        private final Control name = control("localBranch");

        public CheckoutToLocalBranch(GitScm git, String path) {
            super(git, path);
            clickLink("Check out to specific local branch");
        }
    }

    public static class CheckoutToLocalDir extends Behaviour {
        private final Control name = control("relativeTargetDir");

        public CheckoutToLocalDir(GitScm git, String path) {
            super(git, path);
            clickLink("Check out to a sub-directory");
        }
    }

    public static class RecursiveSubmodules extends Behaviour {
        private final Control enable = control("recursiveSubmodules");

        public RecursiveSubmodules(GitScm git, String path) {
            super(git, path);
            clickLink("Advanced sub-modules behaviours");
        }
    }

    public static class AdvancedCheckout extends Behaviour {
        private final Control timeout = control("timeout");

        public AdvancedCheckout(GitScm git, String path) {
            super(git, path);
            clickLink("Advanced checkout behaviours");
        }
    }

    public static class AdvancedClone extends Behaviour {
        private final Control cbShallowClone = control("shallow");
        private final Control numDepth = control("depth");
        private final Control cbNoTags = control("noTags");
        private final Control cbHonorRefspec = control("honorRefspec");
        private final Control txtReference = control("reference");
        private final Control numTimeout = control("timeout");

        public AdvancedClone(GitScm git, String path) {
            super(git, path);
            clickLink("Advanced clone behaviours");
        }
    }

    public static class CalculateChangelog extends Behaviour {
        private final Control txtCompareRemote = control("options/compareRemote");
        private final Control txtCompareTarget = control("options/compareTarget");

        public CalculateChangelog(GitScm git, String path) {
            super(git, path);
            clickLink("Calculate changelog against a specific branch");
        }
    }

    public static class CheckoutLocalBranch extends Behaviour {
        private final Control txtLocalBranch = control("localBranch");

        public CheckoutLocalBranch(GitScm git, String path) {
            super(git, path);
            clickLink("Check out to specific local branch");
        }
    }

    public static class CleanAfterCheckout extends Behaviour {

        public CleanAfterCheckout(GitScm git, String path) {
            super(git, path);
            clickLink("Clean after checkout");
        }
    }

    public static class CleanBeforeCheckout extends Behaviour {

        public CleanBeforeCheckout(GitScm git, String path) {
            super(git, path);
            clickLink("Clean before checkout");
        }
    }

    public static class CreateTagForBuild extends Behaviour {

        public CreateTagForBuild(GitScm git, String path) {
            super(git, path);
            clickLink("Create a tag for every build");
        }
    }

    public static class CustomSCMName extends Behaviour {
        private final Control txtName = control("name");

        public CustomSCMName(GitScm git, String path) {
            super(git, path);
            clickLink("Custom SCM name");
        }
    }

    public static class CustomNameAndMail extends Behaviour {
        private final Control txtName = control("name");
        private final Control txtEmail = control("email");

        public CustomNameAndMail(GitScm git, String path) {
            super(git, path);
            clickLink("Custom user name/e-mail address");
        }
    }

    public static class NoBuildOnCommit extends Behaviour {

        public NoBuildOnCommit(GitScm git, String path) {
            super(git, path);
            clickLink("Don't trigger a build on commit notifications");
        }
    }

    public static class ForcePollingUsingWorkspace extends Behaviour {

        public ForcePollingUsingWorkspace(GitScm git, String path) {
            super(git, path);
            clickLink("Force polling using workspace");
        }
    }

    public static class GitLfsPull extends Behaviour {

        public GitLfsPull(GitScm git, String path) {
            super(git, path);
            clickLink("Git LFS pull after checkout");
        }
    }

    public static class MergeBeforeBuild extends Behaviour {
        private final Control txtMergeRemote = control("mergeRemote");
        private final Control txtMergeTarget = control("mergeTarget");
        private final Control selMergeStrategy = control("mergeStrategy");
        private final Control selFastForwardMode = control("fastForwardMode");

        public MergeBeforeBuild(GitScm git, String path) {
            super(git, path);
            clickLink("Merge before build");
        }
    }

    public static class PollingIgnoresUser extends Behaviour {
        private final Control txtExcludeUsers = control("excludeUsers");

        public PollingIgnoresUser(GitScm git, String path) {
            super(git, path);
            clickLink("Polling ignores commits from certain users");
        }
    }

    public static class PollingIgnoresPath extends Behaviour {
        private final Control txtIncludedRegions = control("includedRegions");
        private final Control txtExcludedRegions = control("excludedRegions");

        public PollingIgnoresPath(GitScm git, String path) {
            super(git, path);
            clickLink("Pollinig ignores commits in certain paths");
        }
    }

    public static class PollingIgnoresMessage extends Behaviour {
        private final Control txtExcludedMessage = control("excludedMessage");

        public PollingIgnoresMessage(GitScm git, String path) {
            super(git, path);
            clickLink("Polling ignores commits with certain messages");
        }
    }

    public static class PruneStableRemoteBranches extends Behaviour {

        public PruneStableRemoteBranches(GitScm git, String path) {
            super(git, path);
            clickLink("Prune stable remote-tracking branches");
        }
    }

    // TODO Check if addPath actually works
    public static class SparseCheckoutPaths extends Behaviour {
        public final ArrayList<Control> paths = new ArrayList<>();
        public final Control btnAdd = control("repeatable-add");

        public SparseCheckoutPaths(GitScm git, String path) {
            super(git, path);
            clickLink("Sparse Checkout paths");
            paths.add(control("sparseCheckoutPaths/path"));
        }

        public Behaviour addPath () {
            String relativePaths = "sparseCheckoutPaths[" + paths.size() + "]/path";
            btnAdd.click();
            paths.add(control(relativePaths));
            return this;
        }
    }

    // TODO Check if selStrategy is correctly referenced
    public static class StrategyToChooseBuild extends Behaviour {
        private final Control selStrategy = control(""); //absolute path is "/scm[1]/extensions/"
        private final Control numMaxAge = control("maximumAgeInDays");
        private final Control txtAncestorCommit = control("ancestorCommitSha1");

        public StrategyToChooseBuild(GitScm git, String path) {
            super(git, path);
            clickLink("Strategy for choosing what to build");
        }
    }

    public static class CommitAuthorInChangelog extends Behaviour {

        public CommitAuthorInChangelog(GitScm git, String path) {
            super(git, path);
            clickLink("Use commit author in changelog");
        }
    }

    public static class WipeAndForceClone extends Behaviour {

        public WipeAndForceClone(GitScm git, String path) {
            super(git, path);
            clickLink("Wipe out repository & force clone");
        }
    }
}
