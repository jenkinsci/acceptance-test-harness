package org.jenkinsci.test.acceptance.po;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildHistory extends PageObject {

    private static final Pattern CONSOLE_LINK_PATTERN = Pattern.compile("/job/(.+?)/(\\d+)/console");

    public BuildHistory(Node parent) {
        super(parent.injector, parent.url("builds"));
    }

    public BuildHistory(View parent) {
        super(parent.injector, parent.url("builds"));
    }

    public Set<Build> getBuilds() {
        open();

        // build history is progressively rendered so wait until that is complete
        WebElement progressiveRendering = findIfNotVisible(by.xpath("//*[@tooltip='Computation in progress.']"));
        waitFor(progressiveRendering).until(we -> !we.isDisplayed());

        LinkedHashSet<Build> builds = new LinkedHashSet<Build>();
        for (WebElement element : all(by.xpath(
                "//a[@href]/span[text() = 'Console output']/.. | " + "//a[@href][img/@alt = 'Console output']"))) {
            String href = element.getAttribute("href");
            java.util.regex.Matcher matcher = CONSOLE_LINK_PATTERN.matcher(href);
            if (!matcher.find()) {
                throw new RuntimeException(href + " does not look like build console log url");
            }

            String jobName = matcher.group(1);
            int buildNumber = Integer.parseInt(matcher.group(2));
            builds.add(getJenkins().jobs.get(Job.class, jobName).build(buildNumber));
        }

        return builds;
    }

    public Set<Build> getBuildsOf(Job... _jobs) {
        List<Job> jobs = Arrays.asList(_jobs);
        LinkedHashSet<Build> builds = new LinkedHashSet<Build>();
        for (Build b : getBuilds()) {
            if (jobs.contains(b.job)) {
                builds.add(b);
            }
        }

        return builds;
    }

    public static Matcher<BuildHistory> containsBuildOf(final Job job) {
        return new Matcher<BuildHistory>("Build history containing a build of %s", job) {
            @Override
            public boolean matchesSafely(BuildHistory item) {
                return !item.getBuildsOf(job).isEmpty();
            }

            @Override
            public void describeMismatchSafely(BuildHistory item, Description mismatchDescription) {
                mismatchDescription.appendValueList("Build history: ", ",", ".", item.getBuilds());
            }
        };
    }
}
