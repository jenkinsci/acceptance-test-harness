package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeTrue;

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
    @Deprecated
    public boolean isInstalled(String pluginShortName) throws InstallationFailedException {
        // look for newer results first as we might retry
        JsonNode[] jobs = Iterables.toArray(getJson("tree=jobs[*[*]]").get("jobs"), JsonNode.class);
        ArrayUtils.reverse(jobs);

        for (JsonNode job : jobs) {
            JsonNode p = job.get("plugin");
            if (p!=null) {
                if (pluginShortName.equals(p.get("name").asText())) {
                    JsonNode errorMessage = p.get("errorMessage");
                    if (errorMessage!=null) {
                        throw new InstallationFailedException(errorMessage.asText());
                    }

                    JsonNode st = job.get("status");
                    if (st.get("success").asBoolean()) {
                        return true;    // successfully installed
                    }
                    JsonNode type = st.get("type");
                    if (type.asText().equals("Failure")) {
                        throw new InstallationFailedException("failed, see log");
                    }

                    return false;   // still in progress
                }
            }
        }

        throw new AssertionError("No record of installation being attempted for "+pluginShortName+"\n"+ asList(jobs));
    }

    /**
     * Wait for the plugin installation is done.
     *
     * Wait for all UC jobs are completed. If some of them fail or require restart, Jenkins is restarted.
     * If some of the plugins is not installed after that or the version is older than expected, the waiting is considered failed.
     *
     * @return true if Jenkins ware restarted to install plugins.
     * @throws InstallationFailedException If the installation has failed.
     */
    public boolean waitForInstallationToComplete(final PluginSpec... specs) throws InstallationFailedException {
        open();
        // Wait for all plugins to get installed
        AtomicInteger i = new AtomicInteger();
        waitFor()
                .withMessage("All plugins should be installed")
                .withTimeout(5, TimeUnit.MINUTES)
                .until(() -> {
                    if (i.incrementAndGet() % 30 == 0) { // refresh the page every 15 seconds
                        driver.navigate().refresh();
                    }
                    return not(Matchers.hasElement(by.xpath("//*[@id='log']//*[contains(.,'Pending') or contains(.,'Installing')]"))).matches(driver);
        });

        String uc = getPageContent(driver);
        // "IOException: Failed to dynamically deploy this plugin" can be reported (by at least some Jenkins versions)
        // in case update of plugin dependency is needed (and is in fact performed in sibling UC job). Restart should fix that.
        boolean restartRequired = uc.contains("restarted") || uc.contains("Failure");

        Jenkins jenkins = getJenkins();
        if (restartRequired) {
            System.out.println("Restarting Jenkins to finish plugin installation");
            jenkins.restart();
        }

        for (PluginSpec spec : specs) {
            Plugin plugin = jenkins.getPlugin(spec.getName());
            if (plugin == null) {
                throw new InstallationFailedException("Plugin " + spec.getName() + " not installed, restarted " + restartRequired);
            }

            if (spec.getVersionNumber() != null && plugin.getVersion().isOlderThan(spec.getVersionNumber())) {
                throw new InstallationFailedException(
                        "Plugin " + spec + " not installed in required version, is " + plugin.getVersion() + ", restarted " + true
                );
            }
        }

        return restartRequired;
    }

    public static class InstallationFailedException extends RuntimeException {
        public InstallationFailedException(String message) {
            super(message);
        }
    }
}
