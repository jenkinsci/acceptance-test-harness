package org.jenkinsci.test.acceptance.po;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.UpdateCenter.InstallationFailedException;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.junit.internal.AssumptionViolatedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.google.common.base.Splitter;
import com.google.inject.Inject;

import hudson.util.VersionNumber;

/**
 * Page object for plugin manager.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginManager extends ContainerPageObject {

    private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());

    /**
     * Did we fetch the update center metadata?
     */
    private boolean updated;

    public final Jenkins jenkins;

    @Inject
    private Provider<UpdateCenterMetadata> ucmd;

    /**
     * Optional configuration value that selects whether to resolve plugins locally and upload to Jenkins
     * (better performing when Jenkins is closer to the test execution), or install plugins from within Jenkins
     * (more accurate testing.)
     */
    @Inject(optional = true)
    @Named("uploadPlugins")
    public Boolean uploadPlugins;

    @Inject(optional = true)
    @Named("forceRestartAfterPluginInstallation")
    public boolean forceRestart;

    public PluginManager(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("pluginManager/"));
        this.jenkins = jenkins;

        // injection happens in the base class, so for us to differentiate default state vs false state,
        // we need to use Boolean
        if (uploadPlugins==null)
            uploadPlugins = true;
    }

    /**
     * Force update the plugin update center metadata.
     */
    public void checkForUpdates() {
        visit("advanced");
        // The check now button is a form submit (POST) with a redirect to the same page only if the check is successful.
        // We use the button itself to detect when the page has changed, which happens after the refresh has been done
        // And we check for the presence of the button again
        clickButton("Check now");
        waitFor(find(by.button("Check now"))).withTimeout(30, TimeUnit.SECONDS).until(new Predicate<WebElement>() {
            // The wait criteria is: we have left the current page and returned to the same one
            @Override
            public boolean apply(@Nullable WebElement webElement) {
                try {
                    try {
                        // We interact with the element just to detect if it is stale
                        webElement.findElement(by.id("it does not matter"));
                    } catch(StaleElementReferenceException e) {
                        // with this exception we know we've left the original page
                        // we look for an element in the page to check for success
                        find(by.button("Check now"));
                        return true;
                    }
                } catch(Exception e) {
                }
                return true;
            }
        });
        updated = true;
    }

    public enum InstallationStatus {NOT_INSTALLED, OUTDATED, UP_TO_DATE}
    /**
     * @param spec plugin id with optional version (e.g. "ldap" or "ldap@1.8")
     * @return whether the plugin (in version greater or equal than specified) is installed
     */
    public InstallationStatus installationStatus(String spec) {
        PluginSpec p = new PluginSpec(spec);
        String name = p.getName();
        String version = p.getVersion();
        Plugin plugin;
        try {
            plugin = jenkins.getPlugin(name);
            if (version != null) {
                VersionNumber actualVersion = plugin.getVersion();
                // check if installed version >= specified version of @WithPlugins
                if (actualVersion.compareTo(new VersionNumber(version)) < 0) {
                    LOGGER.info(name + " has version " + actualVersion + " but " + version + " was requested");
                    return InstallationStatus.OUTDATED;
                }
            }
            LOGGER.info(spec + " is up to date");
            return InstallationStatus.UP_TO_DATE;
        } catch (IllegalArgumentException ex) {
            LOGGER.info(spec + " is not installed");
            return InstallationStatus.NOT_INSTALLED;
        }
    }

    /**
     * @param specs plugin ids with optional version (e.g. "ldap" or "ldap@1.8")
     * @return true, if plugin (in version greater or equal than specified) is installed
     */
    public boolean isInstalled(String... specs) {
        for (String s : specs) {
            if (installationStatus(s) != InstallationStatus.UP_TO_DATE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Installs specified plugins.
     *
     * @deprecated Please be encouraged to use {@link WithPlugins} annotations to statically declare
     * the required plugins you need. If you really do need to install plugins in the middle
     * of a test, as opposed to be in the beginning, then this is the right method.
     * <p/>
     * The deprecation marker is to call attention to {@link WithPlugins}. This method
     * is not really deprecated.
     * @return true if {@link Jenkins#restart} is required
     */
    @Deprecated
    public boolean installPlugins(final String... specs) throws UnableToResolveDependencies {
        boolean changed = false;
        boolean restartRequired = false;
        final Map<String, String> candidates = getMapShortNamesVersion(specs);

        if (uploadPlugins) {
            // First check to see whether we need to do anything.
            // If not, do not consider transitive dependencies of the requested plugins,
            // which might force updates (and thus restarts) even though we already have
            // a sufficiently new version of the requested plugin.
            boolean someChangeRequired = false;
            for (String spec : specs) {
                if (installationStatus(spec) != InstallationStatus.UP_TO_DATE) {
                    someChangeRequired = true;
                    break;
                }
            }
            if (!someChangeRequired) {
                return false;
            }
            List<PluginMetadata> pluginToBeInstalled = ucmd.get().transitiveDependenciesOf(jenkins, candidates);
            for (PluginMetadata newPlugin : pluginToBeInstalled) {
                final String name = newPlugin.getName();
                String claimedVersion = candidates.get(name);
                String availableVersion = newPlugin.getVersion();
                if (claimedVersion == null) { // a dependency
                    claimedVersion = availableVersion;
                }
                final String currentSpec = StringUtils.isNotEmpty(claimedVersion)
                    ? name + "@" + claimedVersion
                    : name
                ;
                InstallationStatus status = installationStatus(currentSpec);
                if (status != InstallationStatus.UP_TO_DATE) {
                    try {
                        if (new VersionNumber(claimedVersion).compareTo(new VersionNumber(availableVersion)) > 0) {
                            throw new AssumptionViolatedException(
                                    name + " has version " + availableVersion + " but " + claimedVersion + " was requested");
                        }
                        newPlugin.uploadTo(jenkins, injector, availableVersion);
                        changed = true;
                        restartRequired |= status == InstallationStatus.OUTDATED;
                        try {
                            new UpdateCenter(jenkins).waitForInstallationToComplete(name);
                        } catch (InstallationFailedException x) {
                            if (!restartRequired) {
                                throw x;
                            }
                            // JENKINS-19859: else ignore; may be fine after the restart
                        }
                    } catch (IOException | ArtifactResolutionException e) {
                        throw new AssertionError("Failed to upload plugin: " + newPlugin, e);
                    }
                }
            }
        } else {
            if (!updated)
                checkForUpdates();

            OUTER:
            for (final String n : candidates.keySet()) {
                for (int attempt = 0; attempt < 2; attempt++) {// # of installations attempted, considering retries
                    visit("available");
                    check(find(by.xpath("//input[starts-with(@name,'plugin.%s.')]", n)));

                    clickButton("Install");

                    elasticSleep(1000);

                    try {
                        new UpdateCenter(jenkins).waitForInstallationToComplete(n);
                        changed = true;
                    } catch (InstallationFailedException e) {
                        if (e.getMessage().contains("Failed to download from")) {
                            continue;   // retry
                        }
                    }

                    continue OUTER;  // installation completed
                }
            }
            // TODO set restartRequired if we did updates
        }

        if (changed) {
            if (restartRequired || forceRestart) {
                return true;
            } else {
                // plugin deployment happens asynchronously, so give it a few seconds
                // for it to finish deploying
                // TODO: Use better detection if this is actually necessary
                // TODO: This is as good as Thread.sleep(5000) if we treat timeout as success
                try {
                    waitFor().withTimeout(4, TimeUnit.SECONDS)
                            .until(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return isInstalled(specs);
                                }
                    });
                } catch (TimeoutException e) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Installs a plugin by uploading the *.jpi image.
     */
    public void installPlugin(File localFile) {
        visit("advanced");
        WebElement form = find(by.name("uploadPlugin"));
        WebElement upload = form.findElement(by.input("name"));
        upload.sendKeys(localFile.getAbsolutePath());
        form.submit();
    }

    /**
     * Generates a map with shortNames and version.
     * Version is null if not declared.
     *
     * @param specs Values of the {@link WithPlugins} annotation
     * @return Map with Key:shortName Value:Version
     */
    private Map<String, String> getMapShortNamesVersion(String... specs) {
        Map<String, String> shortNamesVersion = new HashMap<>();
        for (String s : specs) {
            PluginSpec coord = new PluginSpec(s);
            shortNamesVersion.put(coord.getName(), coord.getVersion());
        }
        return shortNamesVersion;
    }

    /**
     * Reference to a plugin, optionally with the version.
     *
     * The string syntax of this is shortName[@version].
     */
    public static class PluginSpec {
        /**
         * Short name of the plugin.
         */
        private final @Nonnull String name;
        /**
         * Optional version.
         */
        private final String version;

        public PluginSpec(String coordinates) {
            Iterator<String> spliter = Splitter.on("@").split(coordinates).iterator();
            name = spliter.next();
            version = spliter.hasNext()
                    ? spliter.next()
                    : null
            ;
        }

        public @Nonnull String getName() {
            return name;
        }

        public @Nullable String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if (version != null) {
                sb.append('@').append(version);
            }

            return sb.toString();
        }
    }
}
