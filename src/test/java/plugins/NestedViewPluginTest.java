package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.nested_view.NestedView;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins("nested-view")
public class NestedViewPluginTest extends AbstractJUnitTest {

    private FreeStyleJob j;
    private NestedView v;

    @Before
    public void setUp() throws Exception {
        j = jenkins.jobs.create();
        v = jenkins.views.create(NestedView.class,"Nested");
    }

    @Test
    public void create_nested_view() {
        jenkins.open();
        clickLink("Nested");
        assertThat(driver.getCurrentUrl(), endsWith("/view/Nested/"));
    }

    @Test
    public void add_subviews_to_a_nested_view() {
        v.views.create(ListView.class, "list");
        v.views.create(ListView.class, "list2");
        v.open();
        find(by.link("list"));
        find(by.link("list2"));
    }

    @Test
    public void set_default_view_of_a_nested_view() {
        v.views.create(ListView.class, "list");
        v.views.create(ListView.class, "list2");
        v.setDefaultView("list");
        v.open();
        v.assertActiveView("list");
        v.assertInactiveView("list2");
    }
}
