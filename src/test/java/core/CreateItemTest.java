package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.By;

public class CreateItemTest extends AbstractJUnitTest {
    @Test
    @Since("2.6")
    public void duplicate_item_name_displays_error() {
        // create a job with a known name
        jenkins.jobs.create(FreeStyleJob.class, "asdf");

        // go try to create one with the same name
        jenkins.jobs.visit("newJob");

        fillIn("name", "asdf");

        blur(find(By.name("name")));

        // the 'name cannot be empty' message:
        assert(!findIfNotVisible(By.id("itemname-required")).isDisplayed());
        // the 'real' message:
        assert(findIfNotVisible(By.id("itemname-invalid")).isDisplayed());

        assert(!findIfNotVisible(By.id("ok-button")).isEnabled());

        jenkins.jobs.findTypeCaption(FreeStyleJob.class).click();

        // the 'name cannot be empty' message:
        assert(!findIfNotVisible(By.id("itemname-required")).isDisplayed());
        // the 'real' message:
        assert(findIfNotVisible(By.id("itemname-invalid")).isDisplayed());

        assert(!findIfNotVisible(By.id("ok-button")).isEnabled());
    }
}
