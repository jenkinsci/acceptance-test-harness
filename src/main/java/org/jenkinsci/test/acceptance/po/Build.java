package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.plugins.nodelabelparameter.NodeParameter;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Kohsuke Kawaguchi
 */
public class Build extends ContainerPageObject {
    public final Job job;

    private String result;

    /**
     * Console output. Cached.
     */
    private String console;
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

    public Build waitUntilStarted() {
        return waitUntilStarted(0);
    }

    public Build waitUntilStarted(int timeout) {
        job.getJenkins().visit("");
        waitForCond(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return hasStarted();
            }
        },timeout);
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

        waitForCond(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return !isInProgress();
            }
        }, timeout);
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

    public String getConsole() {
        if (console != null) {
            return console;
        }

        visit(getConsoleUrl());

        List<WebElement> a = all(by.xpath("//pre"));
        if (a.size() > 1) {
            console = find(by.xpath("//pre[@id='out']")).getText();
        } else {
            console = a.get(0).getText();
        }

        return console;
    }

    public Build shouldContainsConsoleOutput(String fragment) {
        assertThat(this.getConsole(), Matchers.containsRegexp(fragment, Pattern.MULTILINE));
        return this;
    }

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
            return result;
        }

        waitUntilFinished();
        result = getJson().get("result").asText();
        return result;
    }

    public Artifact getArtifact(String artifact) {
        return new Artifact(this, url("artifact/%s", artifact));
    }

    public Build shouldSucceed() {
        assertThat(this, resultIs("SUCCESS"));
        return this;
    }

    public Build shouldFail() {
        assertThat(this, resultIs("FAILURE"));
        return this;
    }

    public Build shouldAbort() {
        assertThat(this, resultIs("ABORTED"));
        return this;
    }

    public Build shouldBeUnstable() {
        assertThat(this, resultIs("UNSTABLE"));
        return this;
    }

    /**
     * This function tries to assert that the current build is pending for a certain
     * node using a NodeParameter. The node's name has to be specified when calling this method.
     */
    public Build shouldBePendingForNodeParameter(String nodename){
        String paramname = "";
        Parameter param;

        //as a job can have multiple parameters, get the NodeParameter to determine its name
        for (int i = 0; i < this.job.getParameters().size(); i++) {
            param = this.job.getParameters().get(i);
            if ( param  instanceof NodeParameter )
                paramname = param.getName();
        }

        String pendingBuildText = this.getPendingBuildText();
        String expectedText=String.format("(pending—%s is offline) [NodeParameterValue: %s=%s]",nodename,paramname,nodename);
        assertThat(pendingBuildText, containsString(expectedText));
        assertThat(this.hasStarted(), is(false));

        return this;
    }

    /**
     * This function tries to assert that the current build is pending due there are no
     * valid online nodes. The node's name has to be specified when calling this method.
     */
    public Build shouldBeTriggeredWithoutValidOnlineNode(String nodename)
    {
        String pendingBuildText = this.getPendingBuildText();
        String expectedText=String.format("(pending—All nodes of label ‘Job triggered without a valid online node, given where: %s’ are offline)",nodename);
        assertThat(pendingBuildText, containsString(expectedText));
        assertThat(this.hasStarted(), is(false));

        return this;
    }

    /**
     * This function extracts a pending build message out of the build history summary if there is one.
     */
    public String getPendingBuildText(){
        //ensure to be on the job's page otherwise we do not have the build history summary
        // to get their content
        this.job.visit("");

        // pending message comes from the queue, and queue's maintenance is asynchronous to UI threads.
        // so if the original response doesn't contain it, we have to wait for the refersh of the build history.
        // so give it a bigger wait.
        return find(by.xpath("//img[@alt='pending']/../..")).getText();
    }

    private Matcher<Build> resultIs(final String expected) {
        return new Matcher<Build>("Build result %s", expected) {
            @Override
            public boolean matchesSafely(Build item) {
                return item.getResult().equals(expected);
            }

            @Override
            public void describeMismatchSafely(Build item, Description dsc) {
                dsc.appendText("was ").appendText(item.getResult())
                        .appendText(". Console output:\n").appendText(getConsole())
                ;
            }
        };
    }

    public String getNode() {
        String n = getJson().get("builtOn").asText();
        if (n.length() == 0) {
            return "master";
        }
        return n;
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
}
