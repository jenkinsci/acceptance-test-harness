package org.jenkinsci.test.acceptance.po;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.databind.JsonNode;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class Build extends ContainerPageObject {
    public enum Result {SUCCESS, UNSTABLE, FAILURE, ABORTED, NOT_BUILT}

    public final Job job;

    private Result result;

    private boolean success;

    public Build(Job job, int buildNumber) {
        super(job.injector, job.url("%d/", buildNumber));
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
        job.getJenkins().visit("");
        waitFor().withMessage("Next build of %s is started", job)
                .withTimeout(timeout, TimeUnit.SECONDS)
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return hasStarted();
                    }
        });
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
        waitUntilStarted();

        // while waiting, hit the console page, so that during the interactive development
        // one can see what the build is doing
        visit("console");

        waitFor().withMessage("Build %s is finished", this)
                .withTimeout(timeout, TimeUnit.SECONDS)
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

    public boolean isInProgress() {
        if (result != null) {
            return false;
        }
        if (!hasStarted()) {
            return false;
        }

        JsonNode d = getJson();
        return d.get("building").booleanValue() || d.get("result") == null;
    }

    public int getNumber() {
        return getJson().get("number").asInt();
    }

    public URL getConsoleUrl() {
        return url("consoleFull");
    }

    public URL getStatusUrl() {
        return url(Integer.toString(getNumber()));
    }

    public void openStatusPage() {
        visit(getStatusUrl());
    }

    public String getConsole() {
        visit(getConsoleUrl());

        try {
            return IOUtils.toString(url("consoleText").openStream());
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
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

    public Artifact getArtifact(String artifact) {
        return new Artifact(this, artifact);
    }

    public List<Artifact> getArtifacts() {
        WebDriver artifact = visit(url("artifact"));
        List<WebElement> fileList = artifact.findElements(By.cssSelector("table.fileList td:nth-child(2) a"));
        List<Artifact> list = new LinkedList<>();
        for (WebElement el : fileList) {
            if ("a".equalsIgnoreCase(el.getTagName())) {
                list.add(getArtifact(el.getText()));
            }
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
        return new Matcher<Build>("Build result %s", expected) {
            @Override
            public boolean matchesSafely(Build item) {
                return item.getResult().equals(expected.name());
            }

            @Override
            public void describeMismatchSafely(Build item, Description dsc) {
                dsc.appendText("was ").appendText(item.getResult())
                        .appendText(". Console output:\n").appendText(getConsole())
                ;
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
        try {
            IOUtils.toByteArray(url.openStream());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void shouldNotExist() {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            assertThat(con.getResponseCode(), is(404));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }


    public Changes getChanges() {
        final URL changesUrl = url("changes");
        visit(changesUrl);
        return new Changes(this, changesUrl);
    }

    @Override
    public String toString() {
        return job.name + " #" + getNumber();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;

        if (!(other instanceof Build)) return false;

        Build rhs = (Build) other;
        return getNumber() == rhs.getNumber() && job.equals(rhs.job);
    }

    @Override
    public int hashCode() {
        return job.hashCode() + getNumber();
    }
}
