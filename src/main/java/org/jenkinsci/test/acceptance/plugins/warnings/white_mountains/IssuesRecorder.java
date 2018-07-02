package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * Page object for the IssuesRecorder of the warnings plugin (white mountains release).
 *
 * @author Ullrich Hafner
 */
@Describable("Record static analysis results")
public class IssuesRecorder extends AbstractStep implements PostBuildStep {
    private Control toolsRepeatable = control("repeatable-add");
    private Control filtersRepeatable = control("repeatable-add[1]");
    private Control advancedButton = control("advanced-button");
    private Control enabledForFailureCheckBox = control("enabledForFailure");
    private Control ignoreAnalysisResultCheckBox = control("ignoreAnalysisResult");
    private Control overallResultMustBeSuccessCheckBox = control("overallResultMustBeSuccess");
    private Control referenceJobField = control("referenceJob");
    private Control aggregatingResultsCheckBox = control("aggregatingResults");
    private IssueFilterPanel issueFilterPanel;

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
     *
     * @return the sub page of the tool
     */
    public StaticAnalysisTool setTool(final String toolName) {
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "tools");
        tool.setTool(toolName);
        return tool;
    }

    /**
     * Sets the name and the pattern of the static analysis tool to use.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the file name pattern
     */
    public void setTool(final String toolName, final String pattern) {
        setTool(toolName, tool -> tool.setPattern(pattern));
    }

    /**
     * Sets a static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param configuration
     *         the additional configuration options for this tool
     *
     * @return the sub page of the tool
     */
    public StaticAnalysisTool setTool(final String toolName, final Consumer<StaticAnalysisTool> configuration) {
        StaticAnalysisTool tool = setTool(toolName);
        configuration.accept(tool);
        return tool;
    }

    /**
     * Adds a new static analysis tool configuration. The pattern will be empty, i.e. the console log is scanned.
     *
     * @param toolName
     *         the tool name
     *
     * @return the sub page of the tool
     */
    public StaticAnalysisTool addTool(final String toolName) {
        return createToolPageArea(toolName);
    }

    /**
     * Adds a new static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param configuration
     *         the additional configuration options for this tool
     *
     * @return the sub page of the tool
     */
    public StaticAnalysisTool addTool(final String toolName, final Consumer<StaticAnalysisTool> configuration) {
        StaticAnalysisTool tool = addTool(toolName);
        configuration.accept(tool);
        return tool;
    }

    /**
     * Adds a new static analysis tool configuration.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the file name pattern
     *
     * @return the sub page of the tool
     */
    public StaticAnalysisTool addTool(final String toolName, final String pattern) {
        return addTool(toolName, tool -> tool.setPattern(pattern));
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
     * Enables or disables the checkbox 'aggregatingResultsCheckBox'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setEnabledForAggregation(final boolean isChecked) {
        aggregatingResultsCheckBox.check(isChecked);
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
    public static class StaticAnalysisTool extends PageAreaImpl {
        private final Control pattern = control("pattern");
        private final Control normalThreshold = control("tool/normalThreshold");
        private final Control highThreshold = control("tool/highThreshold");

        StaticAnalysisTool(final PageArea issuesRecorder, final String path) {
            super(issuesRecorder, path);
        }

        /**
         * Sets the name of the tool.
         *
         * @param toolName
         *         the name of the tool, e.g. CheckStyle, CPD, etc.
         *
         * @return this
         */
        public StaticAnalysisTool setTool(final String toolName) {
            Select select = new Select(self().findElement(By.className("dropdownList")));
            select.selectByVisibleText(toolName);

            return this;
        }

        /**
         * Sets the pattern of the files to parse.
         *
         * @param pattern
         *         the pattern
         *
         * @return this
         */
        public StaticAnalysisTool setPattern(final String pattern) {
            this.pattern.set(pattern);

            return this;
        }

        /**
         * Sets the normal threshold for duplicate code warnings.
         *
         * @param normalThreshold
         *         threshold to be set
         *
         * @return this
         */
        public StaticAnalysisTool setNormalThreshold(int normalThreshold) {
            this.normalThreshold.set(normalThreshold);

            return this;
        }

        /**
         * Sets the high threshold for duplicate code warnings.
         *
         * @param highThreshold
         *         threshold to be set
         *
         * @return this
         */
        public StaticAnalysisTool setHighThreshold(int highThreshold) {
            this.highThreshold.set(highThreshold);

            return this;
        }
    }

    /**
     * Page area of a issue filter configuration.
     */
    private static class IssueFilterPanel extends PageAreaImpl {
        private final Control regexField = control("pattern");

        IssueFilterPanel(final PageArea area, final String path) {
            super(area, path);
        }

        private void setFilter(final String filter, final String regex) {
            Select filterField = new Select(self().findElement(By.className("dropdownList")));
            filterField.selectByVisibleText(filter);
            regexField.set(regex);
        }

    }

    /**
     * Sets a qualitygate configuration where all sixteen fields are filled with the same value.
     *
     * @param valueForAll
     *         threshold for all configuration fields of qualitygate.
     */
    public void addQualityGateConfiguration(int valueForAll) {
        setUnstableTotalQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
        setFailedTotalQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
        setUnstableNewQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
        setFailedNewQualityGateConfiguration(valueForAll, valueForAll, valueForAll, valueForAll);
    }

    /**
     * Sets the thresholds for the build to be considered as unstable for issues overall.
     *
     * @param total
     *         threshold for overall issues
     * @param high
     *         threshold for issues with high severity
     * @param normal
     *         threshold for issues with normal severity
     * @param low
     *         threshold for issues with low severity
     */
    public void setUnstableTotalQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("_.unstableTotalAll")).set(total);
        control(By.name("_.unstableTotalHigh")).set(high);
        control(By.name("_.unstableTotalNormal")).set(normal);
        control(By.name("_.unstableTotalLow")).set(low);
    }

    /**
     * Sets the thresholds for the build to be considered as failed for issues overall.
     *
     * @param total
     *         threshold for overall issues
     * @param high
     *         threshold for issues with high severity
     * @param normal
     *         threshold for issues with normal severity
     * @param low
     *         threshold for issues with low severity
     */
    void setFailedTotalQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("_.failedTotalAll")).set(total);
        control(By.name("_.failedTotalHigh")).set(high);
        control(By.name("_.failedTotalNormal")).set(normal);
        control(By.name("_.failedTotalLow")).set(low);
    }

    /**
     * Sets the thresholds for the build to be considered as unstable for new issues.
     *
     * @param total
     *         threshold for overall issues
     * @param high
     *         threshold for issues with high severity
     * @param normal
     *         threshold for issues with normal severity
     * @param low
     *         threshold for issues with low severity
     */
    void setUnstableNewQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("_.unstableNewAll")).set(total);
        control(By.name("_.unstableNewHigh")).set(high);
        control(By.name("_.unstableNewNormal")).set(normal);
        control(By.name("_.unstableNewLow")).set(low);
    }

    /**
     * Sets the thresholds for the build to be considered as failed for new issues.
     *
     * @param total
     *         threshold for overall issues
     * @param high
     *         threshold for issues with high severity
     * @param normal
     *         threshold for issues with normal severity
     * @param low
     *         threshold for issues with low severity
     */
    void setFailedNewQualityGateConfiguration(int total, int high, int normal, int low) {
        control(By.name("_.failedNewAll")).set(total);
        control(By.name("_.failedNewHigh")).set(high);
        control(By.name("_.failedNewNormal")).set(normal);
        control(By.name("_.failedNewLow")).set(low);
    }

    /**
     * Adds a new issue filter.
     *
     * @param filterName
     *         name of the filter
     * @param regex
     *         regular expression to apply
     */
    public void addIssueFilter(final String filterName, final String regex) {
        if (issueFilterPanel == null) {
            // fill initial existing filter
            issueFilterPanel = new IssueFilterPanel(this, "filters");
            issueFilterPanel.setFilter(filterName, regex);
        }
        else {
            // create new filter
            String path = createPageArea("filters", () -> filtersRepeatable.click());
            IssueFilterPanel filter = new IssueFilterPanel(this, path);
            filter.setFilter(filterName, regex);
        }
    }

}
