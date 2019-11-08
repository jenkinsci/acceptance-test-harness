package org.jenkinsci.test.acceptance.plugins.mission_control;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link View} class of the mission control plugin.
 */
@Describable("Mission Control")
public class MissionControlView extends View {

    // Every element of the mission control view to configure it properly...
    private Control viewName = new Control(this, "/name");
    private Control viewDescription = new Control(this, "/description");
    private Control filterQueue = new Control(this, "/filterQueue");
    private Control filterExecutors = new Control(this, "/filterExecutors");
    private Control filterJobBuildHistory = new Control(this, "/useRegexFilterBuildHistory");
    private Control filterJobBuildStatuses = new Control(this, "/useRegexFilterJobStatuses");
    private Control fontSize = new Control(this, "/fontSize");
    private Control historySize = new Control(this, "/buildHistorySize");
    private Control queueSize = new Control(this, "/buildQueueSize");
    private Control condensedTables = new Control(this, "/useCondensedTables");
    private Control filterByFailures = new Control(this, "/filterByFailures");
    private Control hideBuildHistory = new Control(this, "/hideBuildHistory");
    private Control hideJobs = new Control(this, "/hideJobs");
    private Control hideBuildQueue = new Control(this, "/hideBuildQueue");
    private Control hideNodes = new Control(this, "/hideNodes");
    private Control statusBtnSize = new Control(this, "/statusButtonSize");
    private Control heightRatio = new Control(this, "/layoutHeightRatio");

    // All page areas contained in the mission control view
    private BuildHistoryArea buildHistoryArea = new BuildHistoryArea(this);
    private BuildQueueArea buildQueueArea = new BuildQueueArea(this);
    private JobStatusArea jobStatusArea = new JobStatusArea(this);
    private NodeStatusArea nodeStatusArea = new NodeStatusArea(this);

    /**
     * Constructor.
     *
     * @param injector Injector.
     * @param url      URL.
     */
    public MissionControlView(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Returns the BuildHistoryArea contained in the current MissionControlView, which offers specific methods to
     * retrieve information from it.
     *
     * @return The {@link BuildHistoryArea} of the current {@link MissionControlView}.
     */
    public BuildHistoryArea getBuildHistoryArea() {
        return buildHistoryArea;
    }

    /**
     * Returns the BuildQueueArea contained in the current MissionControlView, which offers specific methods to
     * retrieve information from it.
     *
     * @return The {@link BuildQueueArea} of the current {@link MissionControlView}.
     */
    public BuildQueueArea getBuildQueueArea() {
        return buildQueueArea;
    }

    /**
     * Returns the JobStatusArea contained in the current MissionControlView, which offers specific methods to
     * retrieve information from it.
     *
     * @return The {@link JobStatusArea} of the current {@link MissionControlView}.
     */
    public JobStatusArea getJobStatusArea() {
        return jobStatusArea;
    }

    /**
     * Returns the NodeStatusArea contained in the current MissionControlView, which offers specific methods to
     * retrieve information from it.
     *
     * @return The {@link NodeStatusArea} of the current {@link MissionControlView}.
     */
    public NodeStatusArea getNodeStatusArea() {
        return nodeStatusArea;
    }

    /**
     * Sets a new name for the view.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param newViewName The new name of the view.
     */
    public void setViewName(String newViewName) {
        ensureConfigPage();
        viewName.set(newViewName);
    }

    /**
     * Sets a new description for the view.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param newViewDescription The new description of the view.
     */
    public void setViewDescription(String newViewDescription) {
        ensureConfigPage();
        viewDescription.set(newViewDescription);
    }

    /**
     * Sets whether or not the queue shall be filtered.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the filter queue option.
     */
    public void setFilterQueue(boolean state) {
        ensureConfigPage();
        filterQueue.check(state);
    }

    /**
     * Sets whether or not the executors shall be filtered.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the filter executors option.
     */
    public void setFilterExecutors(boolean state) {
        ensureConfigPage();
        filterExecutors.check(state);
    }

    /**
     * Sets whether or not the build statuses of jobs shall be filtered.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the filter build status option.
     */
    public void setFilterJobBuildStatuses(boolean state) {
        ensureConfigPage();
        filterJobBuildStatuses.check(state);
    }

    /**
     * Sets whether or not the build history of jobs shall be filtered.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the filter build history option.
     */
    public void setFilterJobBuildHistory(boolean state) {
        ensureConfigPage();
        filterJobBuildHistory.check(state);
    }

    /**
     * Sets the (basic) font size of the view.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param newFontSize The new (basic) font size of the view.
     */
    public void setFontSize(int newFontSize) {
        ensureConfigPage();
        fontSize.set(newFontSize);
    }

    /**
     * Sets the number of records of the history to be displayed.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param numberOfRecords The new number of records to be displayed.
     */
    public void setHistorySize(int numberOfRecords) {
        ensureConfigPage();
        historySize.set(numberOfRecords);
    }

    /**
     * Sets the number of records of the queue to be displayed.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param numberOfRecords The new number of records to be displayed.
     */
    public void setQueueSize(int numberOfRecords) {
        ensureConfigPage();
        queueSize.set(numberOfRecords);
    }

    /**
     * Sets whether or not the tables shall be condensed.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the condensed option.
     */
    public void setCondensedTables(boolean state) {
        ensureConfigPage();
        condensedTables.check(state);
    }

    /**
     * Sets whether or not failed builds shall be filtered.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the filter option for failed builds.
     */
    public void setFilterByFailures(boolean state) {
        ensureConfigPage();
        filterByFailures.check(state);
    }

    /**
     * Sets whether or not the build history shall be hidden.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the hidden build history option.
     */
    public void setHideBuildHistory(boolean state) {
        ensureConfigPage();
        hideBuildHistory.check(state);
    }

    /**
     * Sets whether or not the jobs shall be hidden.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the hidden jobs option.
     */
    public void setHideJobs(boolean state) {
        ensureConfigPage();
        hideJobs.check(state);
    }

    /**
     * Sets whether or not the build queue shall be hidden.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the hidden build queue option.
     */
    public void setHideBuildQueue(boolean state) {
        ensureConfigPage();
        hideBuildQueue.check(state);
    }

    /**
     * Sets whether or not the nodes shall be hidden.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param state The new state of the hidden nodes option.
     */
    public void setHideNodes(boolean state) {
        ensureConfigPage();
        hideNodes.check(state);
    }

    /**
     * Retrieves all possible options of buttons sizes.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @return List of valid button sizes.
     */
    public List<String> getStatusBtnSizes() {
        ensureConfigPage();
        List<String> options = new LinkedList<>();
        for (WebElement e : find(By.xpath("//select[@path='/statusButtonSize']")).findElements(By.tagName("option"))) {
            options.add(e.getText());
        }
        return options;
    }

    /**
     * Sets the size of the status buttons. {@link MissionControlView#getStatusBtnSizes()} returns possible size options.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param sizeOption The new size of the status button.
     */
    public void setStatusBtnSize(String sizeOption) {
        ensureConfigPage();
        statusBtnSize.select(sizeOption);
    }

    /**
     * Retrieves all possible options of height layout ratios.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @return List of valid height layout ratios.
     */
    public List<String> getHeightRatios() {
        ensureConfigPage();
        List<String> options = new LinkedList<>();
        for (WebElement e : find(By.xpath("//select[@path='/layoutHeightRatio']")).findElements(By.tagName("option"))) {
            options.add(e.getText());
        }
        return options;
    }

    /**
     * Sets the layout height ratio. {@link MissionControlView#getHeightRatios()} returns possible ratio options.
     * Calls {@link ConfigurablePageObject#ensureConfigPage()} to ensure that the configuration page of the view is active.
     *
     * @param ratioOption The new ratio of the layout.
     */
    public void setHeightRatio(String ratioOption) {
        ensureConfigPage();
        heightRatio.set(ratioOption);
    }

    /**
     * Reloads the configuration of jenkins from disk.
     * Necessary to display the build history if a new {@link MissionControlView} is created.
     * (Inefficient) alternative: {@code jenkins.restart()}
     */
    public void reloadConfiguration() {
        getJenkins().open();
        driver.findElement(By.xpath("//a[@class='task-link' and @href='/manage']")).click();
        runThenConfirmAlert(() -> driver.findElement(By.xpath("//a[@href='#']")).click());
        getJenkins().waitForLoad(5);
    }

    /**
     * Similar to the {@link ConfigurablePageObject#ensureConfigPage()} method, this method ensures that the
     * view page is open.
     *
     * @return Returns {@code true} if the view page was opened and {@code false} if the the view was already open.
     */
    public boolean ensureViewIsOpen() {
        if (!driver.getCurrentUrl().equals(url.toString())) {
            this.open();
            return true;
        }
        return false;
    }
}
