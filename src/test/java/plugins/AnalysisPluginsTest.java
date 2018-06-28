package plugins;

import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage;
import static org.jenkinsci.test.acceptance.plugins.warnings.assertions.Assertions.*;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.DRYIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings.source_code.SourceCodeView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;


/**
 * Acceptance tests for the White Mountains release of the warnings plug-in.
 *
 * @author Manuel Hampp
 * @author Anna-Maria Hardi
 * @author Stephan Plöderl
 */
@WithPlugins("warnings")
public class AnalysisPluginsTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_plugin/white-mountains/";

    /**
     * Simple test to check that there are some duplicate code warnings.
     */
    @Test
    public void should_have_duplicate_code_warnings() {
        String id = "CPD";
        Build build = createAndBuildFreeStyleJob(id, tool -> {
            tool.setHighThreshold(2);
            tool.setNormalThreshold(1);
        }, "duplicate_code/cpd.xml", "duplicate_code/Main.java");

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();
        assertThat(issuesTable).hasSize(10);
    }

    /**
     * Verifies that clicking on the icon within the details column of the issues table, the row which shows the issues
     * details will be displayed or hidden.
     */
    @Test
    public void should_be_able_to_open_details_row() {
        String id = "CPD";
        Build build = createAndBuildFreeStyleJob(id, tool -> {
            tool.setHighThreshold(2);
            tool.setNormalThreshold(1);
        }, "duplicate_code/cpd.xml", "duplicate_code/Main.java");

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();
        DRYIssuesTableRow firstRow = issuesTable.getRowAs(0, DRYIssuesTableRow.class);
        DRYIssuesTableRow secondRow = issuesTable.getRowAs(1, DRYIssuesTableRow.class);
        assertThat(issuesTable).hasSize(10);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(11);

        DetailsTableRow newSecondRow = issuesTable.getRowAs(1, DetailsTableRow.class);
        assertThat(newSecondRow).hasDetails("public static void functionOne()\n"
                + "  {\n"
                + "    System.out.println(\"testfile for redundancy\");");
        assertThat(issuesTable.getRowAs(2, DRYIssuesTableRow.class)).isEqualTo(secondRow);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(10);
        assertThat(issuesTable.getRowAs(1, DRYIssuesTableRow.class)).isEqualTo(secondRow);
    }

    /**
     * Verifies that the links to the source code view are working.
     * todo replace {@link SourceCodeView} instances by the one written by another team
     */
    @Test
    public void should_be_able_to_open_the_source_code_page_by_clicking_the_links() {
        String id = "CPD";
        String fileName = "Main.java";
        int expectedAmountOfDuplications = 5;
        Build build = createAndBuildFreeStyleJob(id, tool -> {
            tool.setHighThreshold(2);
            tool.setNormalThreshold(1);
        }, "duplicate_code/cpd.xml", "duplicate_code/Main.java");
        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();

        SourceCodeView sourceCodeView = issuesTable.getRowAs(0,DRYIssuesTableRow.class).clickOnFileLink();
        assertThat(sourceCodeView.getFileName()).isEqualTo(fileName);

        issuesTable = page.getIssuesTable();
        DRYIssuesTableRow firstRow = issuesTable.getRowAs(0, DRYIssuesTableRow.class);
        assertThat(firstRow.getDuplicatedIn()).hasSize(expectedAmountOfDuplications);

        for(int i = 0; i < expectedAmountOfDuplications; i++){
            issuesTable = page.getIssuesTable();
            firstRow = issuesTable.getRowAs(0, DRYIssuesTableRow.class);
            sourceCodeView = firstRow.clickOnDuplicatedInLink(i);
            assertThat(sourceCodeView.getFileName()).isEqualTo(fileName);
        }
    }

    /**
     * Verifies that the priority filter link is working.
     */
    @Test
    public void should_be_able_to_use_the_filter_links() {
        String id = "CPD";
        String highPriority = "High";
        String lowPriority = "Low";
        Build build = createAndBuildFreeStyleJob(id, tool -> {
            tool.setHighThreshold(3);
            tool.setNormalThreshold(2);
        }, "duplicate_code/cpd.xml", "duplicate_code/Main.java");

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();

        DRYIssuesTableRow firstRow = issuesTable.getRowAs(0, DRYIssuesTableRow.class);
        assertThat(firstRow).hasPriority(highPriority);
        WarningsResultDetailsPage highPriorityPage = firstRow.clickOnPriorityLink();
        highPriorityPage.getIssuesTable().getTableRows().stream().map(row->row.getAs(AbstractNonDetailsIssuesTableRow.class))
                .forEach(row->assertThat(row).hasPriority(highPriority));

        issuesTable = page.getIssuesTable();
        DRYIssuesTableRow sixthRow = issuesTable.getRowAs(5, DRYIssuesTableRow.class);
        assertThat(sixthRow).hasPriority(lowPriority);
        WarningsResultDetailsPage lowPriorityPage = sixthRow.clickOnPriorityLink();
        lowPriorityPage.getIssuesTable().getTableRows().stream().map(row->row.getAs(AbstractNonDetailsIssuesTableRow.class))
                .forEach(row->assertThat(row).hasPriority(lowPriority));
    }

    /**
     * Creates and builds a FreestyleJob for a specific static analysis tool.
     * @param toolName the name of the tool
     * @param configuration the configuration steps for the static analysis tool
     * @param resourcesToCopy the resources which shall be copied to the workspace
     * @return the finished build
     */
    private Build createAndBuildFreeStyleJob(final String toolName, final Consumer<StaticAnalysisTool> configuration,
            final String... resourcesToCopy) {
        FreeStyleJob job = createFreeStyleJob(resourcesToCopy);
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool(toolName,configuration);
        job.save();

        return job.startBuild().waitUntilFinished();
    }

    /**
     * Opens the WarningsResultDetailsPage and returns the corresponding PageObject representing it.
     * @param id the id of the static analysis tool
     * @param build the build
     * @return the PageObject representing the WarningsResultDetailsPage
     */
    private WarningsResultDetailsPage getWarningsResultDetailsPage(final String id, final Build build) {
        WarningsResultDetailsPage resultPage = new WarningsResultDetailsPage(build, id);
        resultPage.open();
        return resultPage;
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are handled separately if aggregation is not
     * activated.
     */
    @Test
    public void should_log_ok_in_console_with_not_activated_aggregation() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle.xml", "aggregation/pmd.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(false);
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[CheckStyle] Created analysis result for 6 issues");
        assertThat(build.getConsole()).contains("[PMD] Created analysis result for 4 issues");
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are summed up if aggregation is activated.
     */
    @Test
    public void should_log_ok_in_console_with_activated_aggregation() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle.xml", "aggregation/pmd.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(true);
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[Static Analysis] Created analysis result for 10 issues");
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        for (String resource : resourcesToCopy) {
            job.copyResource(resource(WARNINGS_PLUGIN_PREFIX + resource));
        }
        return job;
    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_log_filter_applied_in_console() {
        FreeStyleJob job = createFreeStyleJob("issue_filter/checkstyle-result.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle");
            recorder.openAdvancedOptions();
            recorder.setEnabledForFailure(true);
            recorder.addIssueFilter("Exclude categories", "Checks");
            recorder.addIssueFilter("Include types", "JavadocMethodCheck");
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains(
                "[CheckStyle] Applying 2 filters on the set of 4 issues (3 issues have been removed)");
    }

}

