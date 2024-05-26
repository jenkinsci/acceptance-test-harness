package core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CreateItemTest extends AbstractJUnitTest {

    private static final String JOB_NAME = "asdf";
    private static final String NAME_FIELD = "name";
    private static final String JOB_CREATION_ERROR_MSG = "A job already exists";

    private static final By NAME_NOT_EMPTY_MSG = by.id("itemname-required");
    private static final By EXISTING_NAME_MSG = by.id("itemname-invalid");
    private static final By OK_BUTTON = by.id("ok-button");

    @Test
    @Since("2.6")
    public void duplicate_item_name_displays_error() {
        // create a job with a known name
        jenkins.jobs.create(FreeStyleJob.class, JOB_NAME);

        // go try to create one with the same name
        jenkins.jobs.visit("newJob");
        fillIn(NAME_FIELD, JOB_NAME);
        blur(find(by.name(NAME_FIELD)));

        assertFalse(findIfNotVisible(NAME_NOT_EMPTY_MSG).isDisplayed());
        assertTrue(find(EXISTING_NAME_MSG).isDisplayed());
        assertFalse(find(OK_BUTTON).isEnabled());

        // select type of job
        jenkins.jobs.findTypeCaption(FreeStyleJob.class).click();

        assertFalse(findIfNotVisible(NAME_NOT_EMPTY_MSG).isDisplayed());
        assertTrue(find(EXISTING_NAME_MSG).isDisplayed());

        final WebElement okButtonElement = find(OK_BUTTON);
        // TODO JENKINS-73034
        //assertTrue(okButtonElement.isEnabled());

        okButtonElement.click();
        assertThat(driver, hasContent(JOB_CREATION_ERROR_MSG));
    }
}
