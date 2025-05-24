package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectDoesNotExist;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectExists;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
public class Build extends ContainerPageObject {
    public enum Result {
        SUCCESS,
        UNSTABLE,
        FAILURE,
        ABORTED,
        NOT_BUILT
    }

    public final Job job;

    private Result result;

    private boolean success;

    public Build(Job job, int buildNumber) {
        super(job, job.url("%d/", buildNumber));
        this.job = job;
    }

    public Build(Job job, String permalink) {
        super(job.injector, job.url(permalink + "/"));
        this.job = job;
    }

    public Build(Job job, URL url) {
        super(job.injector, url);
        this.job = job;
    }

    /**
     * "Casts" this object into a subtype by creating the specified type
     */
    public <T extends Build> T as(Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        return newInstance(type, job, url);
    }

    public void delete() {
        visit("confirmDelete");
        waitFor(by.xpath("//span[@name='Submit']"));
        find(by.xpath("//button")).click();
    }

    public Build waitUntilStarted() {
        return waitUntilStarted(120);
    }

    public Build waitUntilStarted(int timeout) {
        waitFor()
                .withMessage("Next build of %s is started", job)
                .withTimeout(Duration.ofSeconds(timeout))
                .until(this::hasStarted);
        return this;
    }

    public boolean hasStarted() {
        if (result != null) {
            return true;
        }

        try {
            getJson();
            // we have json. Build has started.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Build waitUntilFinished() {
        return waitUntilFinished(120);
    }

    public Build waitUntilFinished(int timeout) {
        // Strictly speaking we should reduce the timeout for completion by the time we waited for build to start.
        waitUntilStarted(timeout);

        // while waiting, hit the console page, so that during the interactive development
        // one can see what the build is doing
        visit("console");

        waitFor()
                .withMessage("Build %s is finished", this)
                .withTimeout(Duration.ofSeconds(timeout))
                .until(new Wait.Predicate<Boolean>() {
                    @Override
                    public Boolean apply() throws Exception {
                        return !isInProgress();
                    }

                    @Override
                    public String diagnose(Throwable lastException, String message) {
                        return "Console output:\n" + Build.this.getConsole() + "\n";
                    }
                });
        return this;
    }

    /** Checks if the build is in progress,  that is the build has started and not yet completed. */
    public boolean isInProgress() {
        JsonNode d;
        try {
            d = getJson();
            // see https://github.com/jenkinsci/jenkins/pull/6829

            // for a pipeline you can often see when it has just started "building:true" but "inProgress: false"
            // the former however is set to false whilst post build steps are still firing.
            // so we check both
            return d.get("inProgress").booleanValue() || d.get("building").booleanValue();
        } catch (NoSuchElementException e) {
            // Build has not started, so it is not in progress.
            return false;
        }
    }

    public int getNumber() {
        return getJson().get("number").asInt();
    }

    public URL getConsoleUrl() {
        return url("consoleFull");
    }

    public URL getConsoleTextUrl() {
        return url("consoleText");
    }

    public URL getStatusUrl() {
        return url(Integer.toString(getNumber()));
    }

    public void openStatusPage() {
        visit(getStatusUrl());
    }

    public String getConsole() {
        return visit(getConsoleTextUrl()).findElement(By.tagName("pre")).getText();
    }

    /**
     * @deprecated Use {@link org.jenkinsci.test.acceptance.Matchers#containsRegexp} instead.
     */
    @Deprecated
    public Build shouldContainsConsoleOutput(String fragment) {
        assertThat(this.getConsole(), Matchers.containsRegexp(fragment, Pattern.MULTILINE));
        return this;
    }

    /**
     * @deprecated Use @link{@link org.hamcrest.Matchers#not}({@link org.jenkinsci.test.acceptance.Matchers#containsRegexp}) instead.
     */
    @Deprecated
    public Build shouldNotContainsConsoleOutput(String fragment) {
        assertThat(this.getConsole(), not(Matchers.containsRegexp(fragment, Pattern.MULTILINE)));
        return this;
    }

    public boolean isSuccess() {
        return getResult().equals("SUCCESS");
    }

    /**
     * Returns if the current build is unstable.
     */
    public boolean isUnstable() {
        return getResult().equals("UNSTABLE");
    }

    public String getResult() {
        if (result != null) {
            return result.name();
        }

        waitUntilFinished();
        result = Result.valueOf(getJson().get("result").asText());
        return result.name();
    }

    public Artifact getArtifact(String path) {
        return new Artifact(this, path);
    }

    public List<Artifact> getArtifacts() {
        JsonNode data = getJson("tree=artifacts[*]").get("artifacts");
        List<Artifact> list = new LinkedList<>();
        for (JsonNode e : data) {
            list.add(getArtifact(e.get("relativePath").asText()));
        }
        return list;
    }

    public Build shouldSucceed() {
        assertThat(this, resultIs(Result.SUCCESS));
        return this;
    }

    public Build shouldFail() {
        assertThat(this, resultIs(Result.FAILURE));
        return this;
    }

    public Build shouldAbort() {
        assertThat(this, resultIs(Result.ABORTED));
        return this;
    }

    public Build shouldBeUnstable() {
        assertThat(this, resultIs(Result.UNSTABLE));
        return this;
    }

    public Build shouldBe(final Result result) {
        assertThat(this, resultIs(result));
        return this;
    }

    private Matcher<Build> resultIs(final Result expected) {
        return new Matcher<>("Build result %s", expected) {
            @Override
            public boolean matchesSafely(Build item) {
                return item.getResult().equals(expected.name());
            }

            @Override
            public void describeMismatchSafely(Build item, Description dsc) {
                dsc.appendText("was ")
                        .appendText(item.getResult())
                        .appendText(". Console output:\n")
                        .appendText(getConsole());
            }
        };
    }

    public Node getNode() {
        String n = getJson().get("builtOn").asText();
        if (!n.isEmpty()) {
            return getJenkins().slaves.get(Slave.class, n);
        }

        return getJenkins();
    }

    /**
     * Does this object exist?
     */
    public void shouldExist() {
        assertThat(this, pageObjectExists());
    }

    public void shouldNotExist() {
        assertThat(this, pageObjectDoesNotExist());
    }

    public Changes getChanges() {
        final URL changesUrl = url("changes");
        visit(changesUrl);
        return new Changes(this, changesUrl);
    }

    public void keepForever(boolean keep) {
        open();
        if (keep) {
            clickButton("Keep this build forever");
        } else {
            clickButton("Don't keep this build forever");
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns the human-readable name of this build.
     *
     * @return the name
     */
    public String getName() {
        return job.name + " #" + getNumber();
    }

    public String getDisplayName() {
        WebElement displayNameElement = find(by.xpath("//*[@id=\"main-panel\"]//h1"));
        return displayNameElement.getText();
    }

    /**
     * Stops the build if it's in progress.
     */
    public void stop() {
        open();

        if (isInProgress()) {
            WebElement stopButton = find(by.href("stop"));
            runThenHandleDialog(stopButton::click);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }

        if (!(other instanceof Build rhs)) {
            return false;
        }

        // There is a problem comparing jobs for equality as there is no nice
        // way to access its full name. The default implementation assumes that
        // POs are equal iff they share the same URL which is not true for jobs inside view.
        // return getNumber() == rhs.getNumber() && job.equals(rhs.job);
        return getJson().get("fullDisplayName").equals(rhs.getJson().get("fullDisplayName"));
    }

    @Override
    public int hashCode() {
        return job.hashCode() + getNumber();
    }
}
