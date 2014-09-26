package org.jenkinsci.test.acceptance.po;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.UpdateCenter.InstallationFailedException;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata;
import org.openqa.selenium.TimeoutException;

import com.google.common.base.Splitter;
import com.google.inject.Inject;

import hudson.util.VersionNumber;
import java.util.logging.Logger;
import org.openqa.selenium.WebElement;

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
        visit("checkUpdates");
        waitFor(by.xpath("//span[@id='completionMarker' and text()='Done']"));
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
                // check if installed version >= specified version of @WithPlugins
                if (plugin.getVersion().compareTo(new VersionNumber(version)) < 0) {
                    LOGGER.info(spec + " is out of date");
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
    public boolean installPlugins(final String... specs) {
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
            for (PluginMetadata newPlugin : ucmd.get().transitiveDependenciesOf(candidates.keySet())) {
                final String name = newPlugin.name;
                String claimedVersion = candidates.get(name);
                if (claimedVersion == null) { // a dependency
                    claimedVersion = newPlugin.version;
                }
                String currentSpec;
                if (StringUtils.isNotEmpty(claimedVersion)) {
                    currentSpec = name + "@" + claimedVersion;
                }
                else {
                    currentSpec = name;
                }

                InstallationStatus status = installationStatus(currentSpec);
                if (status != InstallationStatus.UP_TO_DATE) {
                    try {
                        newPlugin.uploadTo(jenkins, injector, null);
                        changed = true;
                        restartRequired |= status == InstallationStatus.OUTDATED;
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
                try {
                    waitForCond(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return isInstalled(specs);
                        }
                    }, 5);
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
