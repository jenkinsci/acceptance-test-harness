package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
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

    /**
     Scenario: Create Nested view
       Given I have installed the "nested-view" plugin
       And a simple job
       When I create a view with a type "Nested View" and name "Nested"
       Then I should see the view on the main page
     */
    @Test
    public void create_nested_view() {
        jenkins.open();
        clickLink("Nested");
        assertThat(driver.getCurrentUrl(), endsWith("/view/Nested/"));
    }

    /**
     Scenario: Add subviews to a Nested view
       Given I have installed the "nested-view" plugin
       And a simple job
       When I create a view with a type "Nested View" and name "Nested"
       And I create a subview of the view with a type "List View" and name "list"
       And I create a subview of the view with a type "List View" and name "list2"
       And I visit the view page
       Then I should see "list" view as a subview of the view
       And I should see "list2" view as a subview of the view
     */
    @Test
    public void add_subviews_to_a_nested_view() {
        v.views.create(ListView.class, "list");
        v.views.create(ListView.class, "list2");
        v.open();
        find(by.link("list"));
        find(by.link("list2"));
    }

    /**
     Scenario: Set default view of a Nested view
      Given I have installed the "nested-view" plugin
      And a simple job
      When I create a view with a type "Nested View" and name "Nested"
      And I create a subview of the view with a type "List View" and name "list"
      And I create a subview of the view with a type "List View" and name "list2"
      And I configure subview "list" as a default of the view
      And I save the view
      And I visit the view page
      Then I should see "list" subview as an active view
      And I should see "list2" subview as an inactive view
     */
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
