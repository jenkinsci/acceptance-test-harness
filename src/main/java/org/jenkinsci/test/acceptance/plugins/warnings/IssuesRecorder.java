package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static java.util.Arrays.asList;

/**
 * Page object for the IssuesRecorder of the warnings plugin (white mountains release).
 *
 * @author Ullrich Hafner
 */
@Describable("Record static analysis results")
public class IssuesRecorder extends AbstractStep implements PostBuildStep {
    private Control toolsRepeatable = control("repeatable-add");
    private Control advancedButton = control("advanced-button");
    private Control enabledForFailureCheckBox = control("enabledForFailure");
    private Control ignoreAnalysisResultCheckBox = control("ignoreAnalysisResult");
    private Control overallResultMustBeSuccessCheckBox = control("overallResultMustBeSuccess");
    private Control referenceJobField = control("referenceJob");

    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     * @param path
     *         path on the parent page
     */
    public IssuesRecorder(final Job parent, final String path) {
        super(parent, path);
    }

    /**
     * Sets the name of the static analysis tool to use.
     *
     * @param toolName
     *         the tool name
     */
    public void setTool(final String toolName) {
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "tools");
        tool.setTool(toolName);
    }

    /**
     * Adds a new static analysis tool configuration. The pattern will be empty, i.e. the console log is scanned.
     *
     * @param toolName
     *         the tool name
     */
    public void addTool(final String toolName) {
        createToolPageArea(toolName);
    }

    /**
     * Adds a new static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the file name pattern
     */
    public void addTool(final String toolName, final String pattern) {
        StaticAnalysisTool tool = createToolPageArea(toolName);
        tool.setPattern(pattern);
    }

    /**
     * Enables or disables the checkbox 'enabledForFailure'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setEnabledForFailure(final boolean isChecked) {
        enabledForFailureCheckBox.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'ignoreAnalysisResult'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setIgnoreAnalysisResult(final boolean isChecked) {
        ignoreAnalysisResultCheckBox.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'overallResultMustBeSuccess'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setOverallResultMustBeSuccess(final boolean isChecked) {
        overallResultMustBeSuccessCheckBox.check(isChecked);
    }

    /**
     * Sets the value of the input field 'referenceJob'.
     *
     * @param referenceJob
     *         the name of the referenceJob
     */
    public void setReferenceJobField(final String referenceJob) {
        referenceJobField.set(referenceJob);
    }

    /**
     * Opens the advanced section.
     */
    public void openAdvancedOptions() {
        advancedButton.click();
    }

    private StaticAnalysisTool createToolPageArea(final String toolName) {
        String path = createPageArea("tools", () -> toolsRepeatable.click());

        StaticAnalysisTool tool = new StaticAnalysisTool(this, path);
        tool.setTool(toolName);
        return tool;
    }

    /**
     * Page area of a static analysis tool configuration.
     */
    private static class StaticAnalysisTool extends PageAreaImpl {
        private final Control pattern = control("pattern");

        StaticAnalysisTool(final PageArea issuesRecorder, final String path) {
            super(issuesRecorder, path);
        }

        public void setTool(final String toolName) {
            Select select = new Select(self().findElement(By.className("dropdownList")));
            select.selectByVisibleText(toolName);
        }

        public void setPattern(final String pattern) {
            this.pattern.set(pattern);
        }
    }

    /**
     * Sets a qualitygate configuration where all sixteen fields are filled with the same value.
     *
     * @param valueForAll
     *         -threshold for all configuration fields of qualitygate.
     */
    public void addQualityGateConfiguration(int valueForAll) {
        unstableTotalQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
        failedTotalQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
        unstableNewQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
        failedNewQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
    }

    /**
     * Sets the thresholds for the build to be considered as unstable for issues overall.
     *
     * @param total
     *         - threshold for overall issues
     * @param high
     *         - threshold for issues with high severity
     * @param normal
     *         - threshold for issues with normal severity
     * @param low
     *         - threshold for issues with low severity
     */
    public void unstableTotalQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("unstableTotalAll")).set(total);
        control(By.name("unstableTotalHigh")).set(high);
        control(By.name("unstableTotalNormal")).set(normal);
        control(By.name("unstableTotalLow")).set(low);
    }

    /**
     * Sets the thresholds for the build to be considered as failed for issues overall.
     *
     * @param total
     *         - threshold for overall issues
     * @param high
     *         - threshold for issues with high severity
     * @param normal
     *         - threshold for issues with normal severity
     * @param low
     *         - threshold for issues with low severity
     */
    void failedTotalQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("failedTotalAll")).set(total);
        control(By.name("failedTotalHigh")).set(high);
        control(By.name("failedTotalNormal")).set(normal);
        control(By.name("failedTotalLow")).set(low);
    }

    /**
     * Sets the thresholds for the build to be considered as unstable for new issues.
     *
     * @param total
     *         - threshold for overall issues
     * @param high
     *         - threshold for issues with high severity
     * @param normal
     *         - threshold for issues with normal severity
     * @param low
     *         - threshold for issues with low severity
     */
    void unstableNewQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("unstableNewAll")).set(total);
        control(By.name("unstableNewHigh")).set(high);
        control(By.name("unstableNewNormal")).set(normal);
        control(By.name("unstableNewLow")).set(low);
    }

    /**
     * Sets the thresholds for the build to be considered as failed for new issues.
     *
     * @param total
     *         - threshold for overall issues
     * @param high
     *         - threshold for issues with high severity
     * @param normal
     *         - threshold for issues with normal severity
     * @param low
     *         - threshold for issues with low severity
     */
    void failedNewQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("failedNewAll")).set(total);
        control(By.name("failedNewHigh")).set(high);
        control(By.name("failedNewNormal")).set(normal);
        control(By.name("failedNewLow")).set(low);
    }

}
