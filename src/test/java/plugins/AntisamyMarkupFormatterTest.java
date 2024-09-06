package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.fail;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

@WithPlugins("antisamy-markup-formatter")
public class AntisamyMarkupFormatterTest extends AbstractJUnitTest {

    private static final String NO_HTML = "safe text with no html";
    private static final String HREF_MESSAGE = "LINK IN DESCRIPTION";
    private static final String HREF_VALID = "http://www.google.com";
    private static final String HREF_INVALID = "javascript:alert(5)";
    private static final String HREF_ELEM = "<a href='%s'>" + HREF_MESSAGE + "</a>";

    @Test
    public void safeHtmlTest() {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        security.selectSafeHtmlFormatter();
        security.save();

        final FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.description(NO_HTML, true);

        assertThat(driver, hasContent(NO_HTML));

        job.description(String.format(HREF_ELEM, HREF_VALID), true);

        assertThat(driver, hasContent(HREF_MESSAGE));
        this.assertHref(HREF_VALID, true);

        job.description(String.format(HREF_ELEM, HREF_INVALID), true);

        assertThat(driver, hasContent(HREF_MESSAGE));
        this.assertHref(HREF_INVALID, false);
    }

    private void assertHref(final String href, final boolean expectsHref) {
        try {
            find(by.href(href));

            if (!expectsHref) {
                fail("Link in description is not sanitized");
            }
        } catch (final NoSuchElementException ex) {
            if (expectsHref) {
                fail("Link in description is not displayed");
            }
        }
    }
}
