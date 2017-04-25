package org.jenkinsci.test.acceptance.plugins.job_dsl;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.*;

/**
 * Encapsulates the PageArea of the Job DSL plugin.
 *
 * @author Maximilian Oeckler
 */
@Describable("Process Job DSLs")
public class JobDSLBuildStep extends AbstractStep implements BuildStep {

    public final Control useScriptText = control(by.radioButton("Use the provided DSL script"));
    public final Control lookOnFilesystem = control(by.radioButton("Look on Filesystem"));

    private final Control targets = control("targets");
    public final Control btExpandTargetsArea = control(by.xpath("//tr[td/input[@id='textarea._.targets' and @name='_.targets']]//input[@type='button']"));
    public final Control ignoreMissingFiles = control("ignoreMissingFiles");
    public final Control ignoreExisting = control("ignoreExisting");

    private final Control removedJobAction = control("removedJobAction");
    private final Control removedViewAction = control("removedViewAction");

    private final Control btAdvanced = control(by.xpath("//td[table[@class='advancedBody']/tbody/tr/td[@class='setting-main']/select[@name='_.lookupStrategy']]/div[@class='advancedLink']//button"));


    public JobDSLBuildStep(Job parent, String path) {
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
        btExpandTargetsArea.click();
        this.targets.set(StringUtils.join(targets, "\n"));
    }

    /**
     * Set what to do when a previously generated job is not referenced anymore.
     * @param action The action to select. A Element of the type {@link JobDSLRemovedJobAction}.
     */
    public void setRemovedJobAction(JobDSLRemovedJobAction action) {
        this.removedJobAction.select(action.toString());
    }

    /**
     * Set what to do when a previously generated view is not referenced anymore..
     * @param action The action to select. A Element of the type {@link JobDSLRemovedViewAction}.
     */
    public void setRemovedViewAction(JobDSLRemovedViewAction action) {
        this.removedViewAction.select(action.toString());
    }

    /**
     * Opens the advanced area of the JobDSL plugin and returns a PageObject
     * of the advanced area of the Job DSL plugin.
     * @return A PageObject of the advanced area of the Job DSL plugin.
     */
    public JobDSLBuildStepAdvanced advanced() {
        btAdvanced.click();
        return this.newInstance(JobDSLBuildStepAdvanced.class, this.getPage(), this.getPage().url);
    }
}
