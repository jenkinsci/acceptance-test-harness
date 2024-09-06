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

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.Scm;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

@Describable("Git")
public class GitScm extends Scm {

    private final Control branch = control("branches/name");
    private final Control url = control("userRemoteConfigs/url");
    private final Control tool = control("gitTool");
    private final Control repositoryBrowser = control("/");
    private final Control urlRepositoryBrowser = control("browser/repoUrl");

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
        addBehaviour(CheckoutToLocalBranch.class).name.set(branch);
        return this;
    }

    public GitScm localDir(String dir) {
        addBehaviour(CheckoutToLocalDir.class).name.set(dir);
        return this;
    }

    public void enableRecursiveSubmoduleProcessing() {
        addBehaviour(RecursiveSubmodules.class).enable.click();
    }

    /**
     * Add behaviour "Calculate changelog against specific branch"
     *
     * @param remote Remote to compare with
     * @param branch Branch to compare with
     * @return this, to allow function chaining
     */
    public GitScm calculateChangelog(String remote, String branch) {
        CalculateChangelog behaviour = addBehaviour(CalculateChangelog.class);
        behaviour.txtCompareRemote.set(remote);
        behaviour.txtCompareTarget.set(branch);
        return this;
    }

    /**
     * Add behaviour "Use commit author in changelog"
     *
     * @return this, to allow function chaining
     */
    public GitScm commitAuthorInChangelog() {
        addBehaviour(CommitAuthorInChangelog.class);
        return this;
    }

    /**
     * Add behaviour "Clean after checkout"
     *
     * @return this, to allow function chaining
     */
    public GitScm cleanAfterCheckout() {
        addBehaviour(CleanAfterCheckout.class);
        return this;
    }

    /**
     * Add behaviour "Clean before checkout"
     *
     * @return this, to allow function chaining
     */
    public GitScm cleanBeforeCheckout() {
        addBehaviour(CleanBeforeCheckout.class);
        return this;
    }

    /**
     * Add behaviour "Create tag for every build"
     *
     * @return this, to allow function chaining
     */
    public GitScm createTagForBuild() {
        addBehaviour(CreateTagForBuild.class);
        return this;
    }

    /**
     * Add behaviour "Custom SCM name"
     *
     * @param name Custom name
     * @return  this, to allow function chaining
     */
    public GitScm customScmName(String name) {
        CustomSCMName behaviour = addBehaviour(CustomSCMName.class);
        behaviour.txtName.set(name);
        return this;
    }

    /**
     * Add behaviour "Custom user name/e-mail address"
     *
     * @param name Custom name
     * @return  this, to allow function chaining
     */
    public GitScm customNameAndMail(String name, String email) {
        CustomNameAndMail behaviour = addBehaviour(CustomNameAndMail.class);
        behaviour.txtName.set(name);
        behaviour.txtEmail.set(email);
        return this;
    }

    /**
     * Add behaviour "Sparse checkout"
     *
     * @return behaviour, to access .addPath() method
     */
    public SparseCheckoutPaths sparseCheckout() {
        return addBehaviour(SparseCheckoutPaths.class);
    }

    /**
     * Add behaviour "Advanced clone behaviours"
     *
     * @return behaviour, to access its method
     */
    public AdvancedClone advancedClone() {
        return addBehaviour(AdvancedClone.class);
    }

    /**
     * Add behaviour "Advanced checkout behaviours"
     *
     * @return behaviour, to access its method
     */
    public AdvancedCheckout advancedCheckout() {
        return addBehaviour(AdvancedCheckout.class);
    }

    /**
     * Select strategy for choosing what to build
     *
     * @param strategy Strategy to use ("Default" || "Inverse")
     * @return this, to allow function chaining
     */
    public GitScm chooseBuildStrategy(String strategy) {
        return chooseBuildStrategy(strategy, 0, null);
    }

    /**
     * Select strategy for choosing what to build
     *
     * @param strategy Strategy to use ("Ancestry" || "Default" || "Inverse")
     * @param age Age in days (only for strategy "Ancestry")
     * @param ancestor SHA1 commit hash (only for strategy "Ancestry")
     * @return this, to allow function chaining
     */
    public GitScm chooseBuildStrategy(String strategy, int age, String ancestor) {
        StrategyToChooseBuild behaviour = addBehaviour(StrategyToChooseBuild.class);
        new Select(behaviour.selStrategy.resolve()).selectByVisibleText(strategy);

        if (strategy.equals("Ancestry")) {
            behaviour.numMaxAge.set(age);
            behaviour.txtAncestorCommit.set(ancestor);
        }

        return this;
    }

    public GitScm remoteName(String name) {
        remoteAdvanced();
        control("userRemoteConfigs/name").set(name);
        return this;
    }

    /**
     * Select repository browser type
     * @param name Type of repository browser
     * @return this, to allow function chaining
     */
    public GitScm repositoryBrowser(String name) {
        Select select = new Select(this.repositoryBrowser.resolve());
        select.selectByVisibleText(name);
        return this;
    }

    /**
     * Set URL for repository browser
     * @param url URL to be set
     * @return this, to allow function chaining
     */
    public GitScm urlRepositoryBrowser(String url) {
        this.urlRepositoryBrowser.set(url);
        return this;
    }

    /**
     * Add behaviour "Merge before build"
     *
     * @return behaviour, to access its method
     */
    public MergeBeforeBuild mergeBeforeBuild() {
        return addBehaviour(MergeBeforeBuild.class);
    }

    public <T extends Behaviour> T addBehaviour(Class<T> type) {
        control("hetero-list-add[extensions]").selectDropdownMenu(type);
        return newInstance(type, this, "extensions"); // FIXME: find the last extension added
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

    @Describable("Check out to specific local branch")
    public static class CheckoutToLocalBranch extends Behaviour {
        private final Control name = control("localBranch");

        public CheckoutToLocalBranch(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Check out to a sub-directory")
    public static class CheckoutToLocalDir extends Behaviour {
        private final Control name = control("relativeTargetDir");

        public CheckoutToLocalDir(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Advanced sub-modules behaviours")
    public static class RecursiveSubmodules extends Behaviour {
        private final Control enable = control("recursiveSubmodules");

        public RecursiveSubmodules(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Advanced checkout behaviours")
    public static class AdvancedCheckout extends Behaviour {
        private final Control timeout = control("timeout");

        public AdvancedCheckout(GitScm git, String path) {
            super(git, path);
        }

        public AdvancedCheckout setTimeOut(String timeOut) {
            this.timeout.set(timeOut);

            return this;
        }
    }

    @Describable("Advanced clone behaviours")
    public static class AdvancedClone extends Behaviour {
        private final Control cbShallowClone = control("shallow");
        private final Control numDepth = control("depth");
        private final Control cbNoTags = control("noTags");
        private final Control cbHonorRefspec = control("honorRefspec");
        private final Control txtReference = control("reference");
        private final Control numTimeout = control("timeout");

        public AdvancedClone(GitScm git, String path) {
            super(git, path);
        }

        public AdvancedClone checkShallowClone(boolean state) {
            cbShallowClone.check(state);

            return this;
        }

        public AdvancedClone checkNoTags(boolean state) {
            cbNoTags.check(state);

            return this;
        }

        public AdvancedClone checkHonorRefspec(boolean state) {
            cbHonorRefspec.check(state);

            return this;
        }

        public AdvancedClone setNumDepth(String numDepth) {
            this.numDepth.set(numDepth);

            return this;
        }

        public AdvancedClone setNumTimeout(String numTimeout) {
            this.numTimeout.set(numTimeout);

            return this;
        }

        public AdvancedClone setTxtReference(String txtReference) {
            this.txtReference.set(txtReference);

            return this;
        }
    }

    @Describable("Calculate changelog against a specific branch")
    public static class CalculateChangelog extends Behaviour {
        private final Control txtCompareRemote = control("options/compareRemote");
        private final Control txtCompareTarget = control("options/compareTarget");

        public CalculateChangelog(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Clean after checkout")
    public static class CleanAfterCheckout extends Behaviour {

        public CleanAfterCheckout(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Clean before checkout")
    public static class CleanBeforeCheckout extends Behaviour {

        public CleanBeforeCheckout(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Create a tag for every build")
    public static class CreateTagForBuild extends Behaviour {

        public CreateTagForBuild(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Custom SCM name")
    public static class CustomSCMName extends Behaviour {
        private final Control txtName = control("name");

        public CustomSCMName(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Custom user name/e-mail address")
    public static class CustomNameAndMail extends Behaviour {
        private final Control txtName = control("name");
        private final Control txtEmail = control("email");

        public CustomNameAndMail(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Don't trigger a build on commit notifications")
    public static class NoBuildOnCommit extends Behaviour {

        public NoBuildOnCommit(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Force polling using workspace")
    public static class ForcePollingUsingWorkspace extends Behaviour {

        public ForcePollingUsingWorkspace(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Git LFS pull after checkout")
    public static class GitLfsPull extends Behaviour {

        public GitLfsPull(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Merge before build")
    public static class MergeBeforeBuild extends Behaviour {
        private final Control txtMergeRemote = control("options/mergeRemote");
        private final Control txtMergeTarget = control("options/mergeTarget");
        private final Control selMergeStrategy = control("options/mergeStrategy");
        private final Control selFastForwardMode = control("options/fastForwardMode");

        public MergeBeforeBuild(GitScm git, String path) {
            super(git, path);
        }

        public MergeBeforeBuild setTxtMergeRemote(String txtMergeRemote) {
            this.txtMergeRemote.set(txtMergeRemote);

            return this;
        }

        public MergeBeforeBuild setTxtMergeTarget(String txtMergeTarget) {
            this.txtMergeTarget.set(txtMergeTarget);

            return this;
        }

        public MergeBeforeBuild setSelMergeStrategy(String selMergeStrategy) {
            this.selMergeStrategy.set(selMergeStrategy);

            return this;
        }

        public MergeBeforeBuild setSelFastForwardMode(String selFastForwardMode) {
            this.selFastForwardMode.set(selFastForwardMode);

            return this;
        }
    }

    @Describable("Polling ignores commits from certain users")
    public static class PollingIgnoresUser extends Behaviour {
        private final Control txtExcludeUsers = control("excludeUsers");

        public PollingIgnoresUser(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Pollinig ignores commits in certain paths")
    public static class PollingIgnoresPath extends Behaviour {
        private final Control txtIncludedRegions = control("includedRegions");
        private final Control txtExcludedRegions = control("excludedRegions");

        public PollingIgnoresPath(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Polling ignores commits with certain messages")
    public static class PollingIgnoresMessage extends Behaviour {
        private final Control txtExcludedMessage = control("excludedMessage");

        public PollingIgnoresMessage(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Prune stable remote-tracking branches")
    public static class PruneStableRemoteBranches extends Behaviour {

        public PruneStableRemoteBranches(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Sparse Checkout paths")
    public static class SparseCheckoutPaths extends Behaviour {
        private int pathCounter = 0;
        private final Control btnAdd = control("repeatable-add");

        public SparseCheckoutPaths(GitScm git, String path) {
            super(git, path);
        }

        public SparseCheckoutPaths addPath(String name) {
            String relativePaths = "sparseCheckoutPaths";

            if (pathCounter == 0) {
                relativePaths += "/path";
            } else {
                btnAdd.click();
                relativePaths += "[" + pathCounter + "]/path";
            }

            control(relativePaths).set(name);
            pathCounter++;
            return this;
        }
    }

    @Describable("Strategy for choosing what to build")
    public static class StrategyToChooseBuild extends Behaviour {
        private final Control selStrategy = control("/"); // absolute path is "/scm[1]/extensions/"
        private final Control numMaxAge = control("buildChooser/maximumAgeInDays");
        private final Control txtAncestorCommit = control("buildChooser/ancestorCommitSha1");

        public StrategyToChooseBuild(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Use commit author in changelog")
    public static class CommitAuthorInChangelog extends Behaviour {

        public CommitAuthorInChangelog(GitScm git, String path) {
            super(git, path);
        }
    }

    @Describable("Wipe out repository & force clone")
    public static class WipeAndForceClone extends Behaviour {

        public WipeAndForceClone(GitScm git, String path) {
            super(git, path);
        }
    }
}
