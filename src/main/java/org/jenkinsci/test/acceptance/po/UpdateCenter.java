package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.Callable;

import static java.util.Arrays.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class UpdateCenter extends ContainerPageObject {
    public UpdateCenter(Jenkins parent) {
        super(parent, parent.url("updateCenter/"));
    }

    /**
     * Has this plugin successfully installed?
     *
     * @return
     *      true if the installation has successfully completed.
     *      false if it's still in progress.
     * @throws InstallationFailedException
     *      installation has failed.
     */
    public boolean isInstalled(String pluginShortName) throws InstallationFailedException {
        // look for newer results first as we might retry
        JsonNode[] jobs = Iterables.toArray(getJson("tree=jobs[*[*]]").get("jobs"), JsonNode.class);
        ArrayUtils.reverse(jobs);

        for (JsonNode job : jobs) {
            JsonNode p = job.get("plugin");
            if (p!=null) {
                if (pluginShortName.equals(p.get("name").asText())) {
                    String errorMessage = p.get("errorMessage").asText();
                    if (errorMessage!=null) {
                        throw new InstallationFailedException(errorMessage);
                    }

                    JsonNode st = p.get("status");
                    if (st.get("success").asBoolean()) {
                        return true;    // successfully installed
                    }

                    return false;   // still in progress
                }
            }
        }

        throw new AssertionError("No record of installation being attempted for "+pluginShortName+"\n"+ asList(jobs));
    }

    /**
     * Blocks until the installation of the specified plugin is ccompleted.
     *
     * @throws InstallationFailedException
     *      If the installation has failed
     */
    public void waitForInstallationToComplete(final String pluginShortName) throws InstallationFailedException {
        waitForCond(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return isInstalled(pluginShortName);
            }
        },180);
    }

    public static class InstallationFailedException extends RuntimeException {
        public InstallationFailedException(String message) {
            super(message);
        }
    }
}
