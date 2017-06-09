package org.jenkinsci.test.acceptance.plugins.job_dsl;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.*;

/**
 * Encapsulates the PageArea of the Job DSL plugin.
 *
 * @author Maximilian Oeckler
 */
@Describable("Process Job DSLs")
public class JobDslBuildStep extends AbstractStep implements BuildStep {

    private final Control useScriptText = control(by.radioButton("Use the provided DSL script"));
    private final Control lookOnFilesystem = control(by.radioButton("Look on Filesystem"));

    private final Control targets = control("targets");
    private final Control expandTargetsArea = control(by.xpath("//tr[td/input[@id='textarea._.targets' and @name='_.targets']]//input[@type='button']"));
    private final Control ignoreMissingFiles = control("ignoreMissingFiles");

    private final Control ignoreExisting = control("ignoreExisting");

    private final Control removedJobAction = control("removedJobAction");
    private final Control removedViewAction = control("removedViewAction");

    private final Control advanced = control(by.xpath("//td[table[@class='advancedBody']/tbody/tr/td[@class='setting-main']/select[@name='_.lookupStrategy']]/div[@class='advancedLink']//button"));

    private final Control lookupStrategy = control("lookupStrategy");
    private final Control failOnMissingPlugin = control("failOnMissingPlugin");
    private final Control unstableOnDeprecation = control("unstableOnDeprecation");

    public JobDslBuildStep(Job parent, String path) {
        super(parent, path);
    }

    /**
     * Set the DSL script.
     * @param dslScript DSL Script, which is groovy code.
     */
    public void setScript(final String dslScript) {
        useScriptText.click();
        new CodeMirror(this, "scriptText").set(dslScript);
    }

    /**
     * Add a newline separated list of DSL scripts.
     * @param targets The DSL scripts, located in the Workspace.
     */
    public void setScriptTargetsOnFilesystem(final String... targets) {
        lookOnFilesystem.click();
        ensureTargetsAreaExpanded();
        this.targets.set(StringUtils.join(targets, "\n"));
    }

    /**
     * Click the radiobutton useScriptText.
     */
    public void clickUseScriptText() {
        useScriptText.click();
    }

    /**
     * Click the radiobutton lookOnFilesystem.
     */
    public void clickLookOnFilesystem() {
        lookOnFilesystem.click();
    }

    /**
     * Decides if missing DSL scripts will be ignored.
     *
     * @param ignore build not fail if true
     */
    public void setIgnoreMissingFiles(boolean ignore) {
        ignoreMissingFiles.check(ignore);
    }

    /**
     * Determines whether checkbox ignoreMissingFiles exists on the current page.
     * @return TRUE if it exists
     */
    public boolean isIgnoreMissingFilesShown() {
        return ignoreMissingFiles.exists();
    }

    /**
     * Decides if previously generated jobs or views will be ignored.
     *
     * @param ignore leave previous jobs and views as is if true
     */
    public void setIgnoreExisting(boolean ignore) {
        ignoreExisting.check(ignore);
    }

    /**
     * Set what to do when a previously generated job is not referenced anymore.
     * @param action The action to select. A Element of the type {@link JobDslRemovedJobAction}.
     */
    public void setRemovedJobAction(JobDslRemovedJobAction action) {
        removedJobAction.select(action.toString());
    }

    /**
     * Set what to do when a previously generated view is not referenced anymore..
     * @param action The action to select. A Element of the type {@link JobDslRemovedViewAction}.
     */
    public void setRemovedViewAction(JobDslRemovedViewAction action) {
        removedViewAction.select(action.toString());
    }

    /**
     * Set the context to use for relative job names.
     * @param strategy The strategy to select. A Element of the type {@link JobDslLookupStrategy}
     */
    public void setLookupStrategy(JobDslLookupStrategy strategy) {
        ensureAdvancedClicked();
        lookupStrategy.select(strategy.toString());
    }

    /**
     * Decides if the build will be marked as failed when a plugin must be installed
     * or updated to support all features used in the DSL scripts.
     *
     * @param fail mark build as failed if true
     */
    public void setFailOnMissingPlugin(boolean fail) {
        ensureAdvancedClicked();
        failOnMissingPlugin.check(fail);
    }

    /**
     * Decides if the build will be marked as unstable when using deprecated features.
     *
     * @param unstable mark build as unstable if true
     */
    public void setUnstableOnDeprecation(boolean unstable) {
        ensureAdvancedClicked();
        unstableOnDeprecation.check(unstable);
    }

    /**
     * Ensures that advanced is clicked and the other controls are visible.
     */
    private void ensureAdvancedClicked() {
        if (advanced.exists()) {
            advanced.click();
        }
    }

    /**
     * Ensures that expandTargetsArea is clicked.
     */
    private void ensureTargetsAreaExpanded() {
        if (expandTargetsArea.exists()) {
            expandTargetsArea.click();
        }
    }
}
