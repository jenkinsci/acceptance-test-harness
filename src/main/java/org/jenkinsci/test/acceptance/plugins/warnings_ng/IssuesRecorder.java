package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.util.function.Consumer;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Page object for the IssuesRecorder of the warnings plugin (white mountains release).
 *
 * @author Ullrich Hafner
 */
@Describable("Record compiler warnings and static analysis results")
public class IssuesRecorder extends AbstractStep implements PostBuildStep {
    private Control toolsRepeatable = findRepeatableAddButtonFor("tools");
    private Control filtersRepeatable = findRepeatableAddButtonFor("filters");
    private Control qualityGatesRepeatable = findRepeatableAddButtonFor("qualityGates");
    private Control advancedButton = control("advanced-button");
    private Control enabledForFailureCheckBox = control("enabledForFailure");
    private Control ignoreAnalysisResultCheckBox = control("ignoreAnalysisResult");
    private Control overallResultMustBeSuccessCheckBox = control("overallResultMustBeSuccess");
    private Control referenceJobField = control("referenceJob");
    private Control aggregatingResultsCheckBox = control("aggregatingResults");
    private Control sourceCodeEncoding = control("sourceCodeEncoding");

    /**
     * Returns the repeatable add button for the specified property.
     *
     * @param propertyName
     *         the name of the repeatable property
     *
     * @return the selected repeatable add button
     */
    protected Control findRepeatableAddButtonFor(final String propertyName) {
        return control(by.xpath("//div[@id='" + propertyName + "']//button[contains(@path,'-add')]"));
    }

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

        ScrollerUtil.hideScrollerTabBar(driver);
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
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
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
     *
     * @return the sub page of the tool
     */
    public StaticAnalysisTool setTool(final String toolName, final String pattern) {
        return setTool(toolName, tool -> tool.setPattern(pattern));
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
     * Sets the source code encoding to the specified value.
     *
     * @param encoding
     *         the encoding to use when reading source files
     */
    public void setSourceCodeEncoding(final String encoding) {
        openAdvancedOptions();

        this.sourceCodeEncoding.set(encoding);
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
     * Enables or disables the checkbox 'aggregatingResultsCheckBox'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setEnabledForAggregation(final boolean isChecked) {
        aggregatingResultsCheckBox.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'ignoreAnalysisResult'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setIgnoreAnalysisResult(final boolean isChecked) {
        openAdvancedOptions();

        ignoreAnalysisResultCheckBox.check(isChecked);
    }

    /**
     * Enables or disables the checkbox 'overallResultMustBeSuccess'.
     *
     * @param isChecked
     *         determines if the checkbox should be checked or not
     */
    public void setOverallResultMustBeSuccess(final boolean isChecked) {
        openAdvancedOptions();

        overallResultMustBeSuccessCheckBox.check(isChecked);
    }

    /**
     * Sets the value of the input field 'referenceJob'.
     *
     * @param referenceJob
     *         the name of the referenceJob
     */
    public void setReferenceJobField(final String referenceJob) {
        openAdvancedOptions();

        referenceJobField.set(referenceJob);
    }

    /**
     * Opens the advanced section.
     */
    private void openAdvancedOptions() {
        if (advancedButton != null && advancedButton.exists()) {
            advancedButton.click();
        }
    }

    private StaticAnalysisTool createToolPageArea(final String toolName) {
        String path = createPageArea("toolProxies", () -> toolsRepeatable.click());

        StaticAnalysisTool tool = new StaticAnalysisTool(this, path);
        tool.setTool(toolName);
        return tool;
    }

    /**
     * Sets the name of the static analysis tool to use and the pattern.
     *
     * @param toolName
     *         the tool name
     * @param pattern
     *         the pattern
     */
    public void setToolWithPattern(final String toolName, final String pattern) {
        StaticAnalysisTool tool = new StaticAnalysisTool(this, "toolProxies");
        tool.setTool(toolName);
        tool.setPattern(pattern);
    }

    /**
     * Adds a new quality gate.
     *
     * @param threshold
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param isUnstable
     *         determines whether the quality gate sets the build result to Unstable or Failed
     */
    public void addQualityGateConfiguration(final int threshold, final QualityGateType type, final boolean isUnstable) {
        String path = createPageArea("qualityGates", () -> qualityGatesRepeatable.click());
        QualityGatePanel qualityGate = new QualityGatePanel(this, path);
        qualityGate.setThreshold(threshold);
        qualityGate.setType(type);
        qualityGate.setUnstable(isUnstable);
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
        String path = createPageArea("filters", () -> filtersRepeatable.selectDropdownMenu(filterName));
        IssueFilterPanel filter = new IssueFilterPanel(this, path);
        filter.setFilter(regex);
    }

    /**
     * Available quality gate types.
     */
    public enum QualityGateType {
        /** Total number of issues. */
        TOTAL("Total number of issues (any severity)"),
        /** Total number of issues (severity Error). */
        TOTAL_ERROR("Total number of errors"),
        /** Total number of issues (severity Warning High). */
        TOTAL_HIGH("Total number of warnings (severity high)"),
        /** Total number of issues (severity Warning Normal). */
        TOTAL_NORMAL("Total number of warnings (severity normal)"),
        /** Total number of issues (severity Warning Low). */
        TOTAL_LOW("Total number of warnings (severity low)"),

        /** Number of new issues. */
        NEW("Number of new issues (any severity)"),
        /** Number of new issues (severity Error). */
        NEW_ERROR("Number of new errors"),
        /** Number of new issues (severity Warning High). */
        NEW_HIGH("Number of new warnings (severity high)"),
        /** Number of new issues (severity Warning Normal). */
        NEW_NORMAL("Number of new warnings (severity normal)"),
        /** Number of new issues (severity Warning Low). */
        NEW_LOW("Number of new warnings (severity low)"),

        /** Delta current build - reference build. */
        DELTA("Delta (any severity)"),
        /** Delta current build - reference build (severity Error). */
        DELTA_ERROR("Delta of errors"),
        /** Delta current build - reference build (severity Warning High). */
        DELTA_HIGH("Delta of warnings (severity high)"),
        /** Delta current build - reference build (severity Warning Normal). */
        DELTA_NORMAL("Delta of warnings (severity normal)"),
        /** Delta current build - reference build (severity Warning Low). */
        DELTA_LOW("Delta of warnings (severity low)");

        private final String displayName;

        QualityGateType(final String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the localized human readable name of this type.
         *
         * @return human readable name
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Page area of a static analysis tool configuration.
     */
    public static class StaticAnalysisTool extends PageAreaImpl {
        private final Control pattern = control("tool/pattern");
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
     * Page area of a filter configuration.
     */
    private static class IssueFilterPanel extends PageAreaImpl {
        private final Control regexField = control("pattern");

        IssueFilterPanel(final PageArea area, final String path) {
            super(area, path);
        }

        private void setFilter(final String regex) {
            regexField.set(regex);
        }
    }

    /**
     * Page area of a quality gate configuration.
     */
    private static class QualityGatePanel extends PageAreaImpl {
        private final Control threshold = control("threshold");
        private final Control type = control("type");
        private final Control unstable = control("unstable");

        QualityGatePanel(final PageArea area, final String path) {
            super(area, path);
        }

        public void setThreshold(final int threshold) {
            this.threshold.set(threshold);
        }

        public void setType(final QualityGateType type) {
            this.type.select(type.getDisplayName());
        }

        public void setUnstable(final boolean isUnstable) {
            find(by.xpath("//input[@type='radio' and contains(@path,'unstable[" + isUnstable + "]')]")).click();
        }
    }
}
