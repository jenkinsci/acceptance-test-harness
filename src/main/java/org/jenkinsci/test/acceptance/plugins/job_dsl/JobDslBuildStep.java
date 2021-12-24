package org.jenkinsci.test.acceptance.plugins.job_dsl;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.By;

/**
 * Encapsulates the PageArea of the Job DSL plugin.
 *
 * @author Maximilian Oeckler
 */
@Describable("Process Job DSLs")
public class JobDslBuildStep extends AbstractStep implements BuildStep {

    private final CodeMirror scriptText = new CodeMirror(this, "scriptText");
    private final Control useScriptText = control(by.radioButton("Use the provided DSL script"));
    private final Control lookOnFilesystem = control(by.radioButton("Look on Filesystem"));

    private final Control targets = control("targets");
    private final Control expandTargetsArea = control(by.xpath("//div[div/input[@id='textarea._.targets' and @name='_.targets']]//input[@type='button']"));
    private final Control ignoreMissingFiles = control("ignoreMissingFiles");

    private final Control useSandbox = control("sandbox");
    private final Control ignoreExisting = control("ignoreExisting");

    private final Control removedJobAction = control("removedJobAction");
    private final Control removedViewAction = control("removedViewAction");
    private final Control removedConfigFilesAction = control("removedConfigFilesAction");

    private final Control advanced = control(by.path("/builder/advanced-button"));

    private final Control lookupStrategy = control("lookupStrategy");
    private final Control additionalClasspath = control("additionalClasspath");
    private final Control expandClasspathArea = control(by.xpath("//div[div/input[@id='textarea._.additionalClasspath' and @name='_.additionalClasspath']]//input[@type='button']"));

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
        scriptText.set(dslScript);
    }

    /**
     * Get the DSL script.
     * @return the DSL script.
     */
    public String getScript() {
        return scriptText.get();
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
     * Get the DSL scripts, that are located in the workspace.
     * @return an array of DSL scripts.
     */
    public String[] getScriptTargetsOnFilesystem() {
        return targets.get().split("\n");
    }

    /**
     * Click the radiobutton useScriptText.
     */
    public void clickUseScriptText() {
        useScriptText.click();
    }

    /**
     * Determines if the provided DSL script should be used.
     * @return TRUE if radiobutton useScriptText is selected
     */
    public boolean isUseScriptText() {
        return useScriptText.resolve().findElement(by.xpath(CapybaraPortingLayerImpl.LABEL_TO_INPUT_XPATH)).isSelected();
    }

    /**
     * Click the radiobutton lookOnFilesystem.
     */
    public void clickLookOnFilesystem() {
        lookOnFilesystem.click();
    }

    /**
     * Determines whether to look on filesystem for DSL scripts.
     * @return TRUE if radiobutton lookOnFilesystem is selected
     */
    public boolean isLookOnFilesystem() {
        return lookOnFilesystem.resolve().findElement(by.xpath(CapybaraPortingLayerImpl.LABEL_TO_INPUT_XPATH)).isSelected();
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
     * Determines if missing DSL scripts will be ignored.
     * @return TRUE if checkbox ignoreMissingFiles is selected
     */
    public boolean isIgnoreMissingFiles() {
        return ignoreMissingFiles.resolve().isSelected();
    }

    /**
     * Determines whether checkbox ignoreMissingFiles exists on the current page.
     * @return TRUE if it exists
     */
    public boolean isIgnoreMissingFilesShown() {
        return ignoreMissingFiles.exists();
    }

    /**
     * Decides if the DSL scripts run in a sandbox with limited abilities.
     *
     * @param use Run DSL scripts in a sandbox if true
     */
    public void setUseSandbox(boolean use) {
        useSandbox.check(use);
    }

    /**
     * Determines if the DSL scripts run in a sandbox with limited abilities.
     * @return TRUE if checkbox sandbox is selected
     */
    public boolean isUseSandbox() {
        return useSandbox.resolve().isSelected();
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
     * Determines if previously generated jobs or views will be ignored.
     * @return TRUE if checkbox ignoreExisting is selected
     */
    public boolean isIgnoreExisting() {
        return ignoreExisting.resolve().isSelected();
    }

    /**
     * Set what to do when a previously generated job is not referenced anymore.
     * @param action The action to select. An Element of the type {@link JobDslRemovedJobAction}.
     */
    public void setRemovedJobAction(JobDslRemovedJobAction action) {
        removedJobAction.select(action.toString());
    }

    /**
     * Determines what to do when a previously generated job is not referenced anymore.
     * @return The selected action for removed jobs. An Element of the type {@link JobDslRemovedJobAction}.
     */
    public JobDslRemovedJobAction getRemovedJobAction() {
        return JobDslRemovedJobAction.valueOf(removedJobAction.get());
    }

    /**
     * Set what to do when a previously generated view is not referenced anymore.
     * @param action The action to select. An element of the type {@link JobDslRemovedViewAction}.
     */
    public void setRemovedViewAction(JobDslRemovedViewAction action) {
        removedViewAction.select(action.toString());
    }

    /**
     * Determines what to do when a previously generated view is not referenced anymore.
     * @return The selected action for removed views. An element of the type {@link JobDslRemovedViewAction}.
     */
    public JobDslRemovedViewAction getRemovedViewAction() {
        return JobDslRemovedViewAction.valueOf(removedViewAction.get());
    }

    /**
     * Set what to do when a previously generated config file is not referenced anymore.
     * @param action The action to select. An element of the type {@link JobDslRemovedConfigFilesAction}.
     */
    public void setRemovedConfigFilesAction(JobDslRemovedConfigFilesAction action) {
        removedConfigFilesAction.select(action.toString());
    }

    /**
     * Determines what to do when a previously generated config file is not referenced anymore.
     * @return The selected action for removed config files. An element of the type {@link JobDslRemovedConfigFilesAction}.
     */
    public JobDslRemovedConfigFilesAction getRemovedConfigFilesAction() {
        return JobDslRemovedConfigFilesAction.valueOf(removedConfigFilesAction.get());
    }

    /**
     * Set the context to use for relative job names.
     * @param strategy The strategy to select. An element of the type {@link JobDslLookupStrategy}.
     */
    public void setLookupStrategy(JobDslLookupStrategy strategy) {
        ensureAdvancedClicked();
        lookupStrategy.select(strategy.toString());
    }

    /**
     * Determines the context to use for relative job names.
     * @return The selected strategy for lookup. An element of the type {@link JobDslLookupStrategy}.
     */
    public JobDslLookupStrategy getLookupStrategy() {
        ensureAdvancedClicked();
        return JobDslLookupStrategy.valueOf(lookupStrategy.get());
    }

    /**
     * Newline separated list of additional classpath entries for the Job DSL scripts.
     * All entries must be relative to the workspace root.
     * @param classpaths The additional classpaths.
     */
    public void setAdditionalClasspath(final String... classpaths) {
        ensureAdvancedClicked();
        ensureClasspathAreaExpanded();
        additionalClasspath.set(StringUtils.join(classpaths, "\n"));

    }

    /**
     * Get the additional classpath entries for the Job DSL scripts.
     * @return an array of additional classpaths.
     */
    public String[] getAdditionalClasspath() {
        ensureAdvancedClicked();
        return additionalClasspath.get().split("\n");
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
     * Determines if the build will be marked as failed when a plugin must be installed
     * or updated to support all features used in the DSL scripts.
     * @return TRUE if checkbox failOnMissingPlugin is selected
     */
    public boolean isFailOnMissingPlugin() {
        ensureAdvancedClicked();
        return failOnMissingPlugin.resolve().isSelected();
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
     * Determines if the build will be marked as unstable when using deprecated features.
     * @return TRUE if checkbox unstableOnDeprecation is selected
     */
    public boolean isUnstableOnDeprecation() {
        ensureAdvancedClicked();
        return unstableOnDeprecation.resolve().isSelected();
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

    /**
     * Ensures that expandClasspathArea is clicked.
     */
    private void ensureClasspathAreaExpanded() {
        if (expandClasspathArea.exists()) {
            expandClasspathArea.click();
        }
    }
}
