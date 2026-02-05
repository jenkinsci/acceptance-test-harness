package core;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertFalse;

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
    private static final String NAME_NOT_EMPTY_MSG = "This field cannot be empty, please enter a valid name";
    private static final String EXISTING_NAME_MSG = "A job already exists with the name ‘asdf’";

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

        assertThat(driver, not(hasContent(NAME_NOT_EMPTY_MSG)));
        assertThat(driver, hasContent(EXISTING_NAME_MSG));
        assertFalse(find(OK_BUTTON).isEnabled());

        // select type of job
        jenkins.jobs.findTypeCaption(FreeStyleJob.class).click();

        assertThat(driver, not(hasContent(NAME_NOT_EMPTY_MSG)));
        assertThat(driver, hasContent(EXISTING_NAME_MSG));

        final WebElement okButtonElement = find(OK_BUTTON);
        okButtonElement.click();
        assertThat(driver, hasContent(JOB_CREATION_ERROR_MSG));
    }
}
