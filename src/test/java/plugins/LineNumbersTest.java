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

import static org.junit.Assert.assertTrue;

/**
 * Tests for the linenumbers plug-in.
 */
@WithPlugins({"linenumbers", "workflow-aggregator"})
public class LineNumbersTest extends AbstractJUnitTest {

    /**
     * Checks that a empty line is not in the same line as the following line.
     */
    @Test @Issue("JENKINS-33105")
    public void preserve_empty_lines() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {\n"
             + "echo ''\n"
             + "echo ' '\n"
             + "echo '  '\n"
             + "echo '''\n"
             + "'''\n"
             + "}\n");
        job.save();
        Build build = job.startBuild().waitUntilFinished();
        build.open();
        driver.findElement(By.partialLinkText("Console Output")).click();
        elasticSleep(1000);
        
        for (int i = 1; i < 14; i++) {
            checkElementsNotInSameLine("L" + Integer.toString(i),
                "L" + Integer.toString(i + 1));
        }
    }
        
    public void checkElementsNotInSameLine(String firstId, String secondId) { 
        WebElement firstLine = driver.findElement(By.id(firstId));
        WebElement secondLine = driver.findElement(By.id(secondId));
        Coordinates firstCoordinates = ((Locatable) firstLine).getCoordinates();
        Coordinates secondCoordinates = ((Locatable) secondLine).getCoordinates();
        Point firstPoint = firstCoordinates.inViewPort();
        Point secondPoint = secondCoordinates.inViewPort();
        assertTrue(firstPoint.getY() < secondPoint.getY());
    }
}
