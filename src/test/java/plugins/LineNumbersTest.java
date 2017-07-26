package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.openqa.selenium.Point;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the linenumbers plug-in.
 */
@WithPlugins({"linenumbers", "workflow-aggregator"})
public class LineNumbersTest extends AbstractJUnitTest {

    /**
     * Checks that a the target line is visible after clicking it.
     */
    @Test @Issue("JENKINS-41284")
    public void scroll_to_see_target_line() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {\n"
            + "for (i = 0; i <200; i++) {\n"
            + "echo 'Hello World'\n"
            + "}\n"
            + "}\n");
        job.save();
        Build build = job.startBuild().waitUntilFinished();
        build.open();

        driver.findElement(By.partialLinkText("Console Output")).click();
        WebElement target = driver.findElement(By.id("L100"));
        target.click();
        elasticSleep(1000);
 
        Coordinates coordinates = ((Locatable) target).getCoordinates();
        Point point = coordinates.inViewPort();
		WebElement breadcrumbBar = driver.findElement(By.id("breadcrumbBar"));
        assertEquals(breadcrumbBar.getSize().getHeight(), point.getY());
    }
}
