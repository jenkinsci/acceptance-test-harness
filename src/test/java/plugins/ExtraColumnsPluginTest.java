package plugins;

import java.util.List;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.extra_columns.LastConsoleColumn;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.jenkinsci.test.acceptance.po.PageObject.*;

/**
 * Acceptance tests for the Extra Columns plugin.
 *
 * @author Ullrich Hafner
 */
@WithPlugins("extra-columns")
public class ExtraColumnsPluginTest extends AbstractJUnitTest {
    /**
     * Sets up a job within a folder. Starts a succeeding build. Adds the last console column to a new view
     * and verifies that clicking the link to the last console opens the console log.
     */
    @Test @WithPlugins("cloudbees-folder")
    public void should_show_console_link_in_folder() {
        Folder folder = jenkins.jobs.create(Folder.class, createRandomName());
        folder.save();
        folder.open();

        FreeStyleJob job = folder.getJobs().create();
        job.startBuild().waitUntilFinished().shouldSucceed();

        ListView.createWithColumn(folder, LastConsoleColumn.class);

        List<WebElement> links = all(by.link("Last/current build console output"));
        assertThat(links, iterableWithSize(1));

        links.get(0).click();

        assertThat(driver, hasContent("Console Output"));
    }
}
