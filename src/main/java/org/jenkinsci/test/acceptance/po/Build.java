package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.NoSuchElementException;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Build extends ContainerPageObject {
    public final int buildNumber;
    public final Job job;

    private String result;

    /**
     * Console output. Cached.
     */
    private String console;
    private boolean success;

    public Build(Job job, int buildNumber) throws Exception {
        super(job.injector,new URL(job.url,String.valueOf(buildNumber)+"/"));
        this.buildNumber = buildNumber;
        this.job = job;
    }

    public Build(Job job, String permalink) throws Exception {
        super(job.injector,new URL(job.url,permalink+"/"));
        this.buildNumber = -1;  // HACK
        this.job = job;
    }

    public Build waitUntilStarted() throws Exception {
        waitForCond(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return hasStarted();
            }
        });
        return this;
    }

    public boolean hasStarted() {
        if (result!=null)
            return true;

        try {
            getJson();
            // we have json. Build has started.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Build waitUntilFinished() throws Exception {
        waitUntilStarted();
        waitForCond(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !isInProgress();
            }
        });
        return this;
    }

    public boolean isInProgress() throws Exception {
        if (result!=null)   return false;
        if (!hasStarted())  return false;

        JsonNode d = getJson();
        return d.get("building").booleanValue() || d.get("result")==null;
    }

    public URL getConsoleUrl() throws Exception {
        return new URL(url,"console");
    }

    public String getConsole() throws Exception {
        if (console!=null)  return console;

        visit(getConsoleUrl());
        try {
            console = find(by.xpath("//pre[@id='out']")).getText();
        } catch (NoSuchElementException _) {
            console = find(by.xpath("//pre")).getText();
        }
        return console;
    }

    public boolean isSuccess() throws Exception {
        return getResult().equals("SUCCESS");
    }

    private String getResult() throws Exception {
        if (result!=null)   return result;

        waitUntilFinished();
        result = getJson().get("result").asText();
        return result;
    }

    public Artifact getArtifact(String artifact) throws Exception {
        return new Artifact(this,new URL(url,"artifact/"+artifact));
    }
}
