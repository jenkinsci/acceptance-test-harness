package org.jenkinsci.test.acceptance.po;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.UpdateCenter.InstallationFailedException;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata;

import com.google.common.base.Splitter;
import com.google.inject.Inject;

/**
 * Page object for plugin manager.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginManager extends ContainerPageObject {
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
    public boolean uploadPlugins = true;

    public PluginManager(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("pluginManager/"));
        this.jenkins = jenkins;
    }

    /**
     * Force update the plugin update center metadata.
     */
    public void checkForUpdates() {
        visit("checkUpdates");
        waitFor(by.xpath("//span[@id='completionMarker' and text()='Done']"));
        updated = true;

        waitForUpdates();
    }

    public void waitForUpdates() {
        JenkinsLogger l = jenkins.getLogger("all");

        Pattern ant = Pattern.compile(".*hudson.tasks.Ant.AntInstaller");
        Pattern maven = Pattern.compile(".*hudson.tasks.Maven.MavenInstaller");
        Pattern jdk = Pattern.compile(".*hudson.tools.JDKInstaller");
        l.waitForLogged(ant);
        l.waitForLogged(maven);
        l.waitForLogged(jdk);
    }

    public boolean isInstalled(String... shortNames) {
        for (String n : shortNames) {
            String name = new PluginCoordinates(n).getName();
            Plugin plugin;
            try {
                plugin = jenkins.getPlugin(n);
            } catch (IllegalArgumentException ex) {
                return false; // Not installed at all
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
     */
    @Deprecated
    public void installPlugins(String... shortNames) {
        final Map<String, String> candidates = getMapShortNamesVersion(shortNames);

        if (uploadPlugins) {
            for (PluginMetadata newPlugin : ucmd.get().transitiveDependenciesOf(candidates.keySet())) {
                final String name = newPlugin.name;
                final String claimedVersion = candidates.get(name);
                try {
                    if (!isInstalled(name) || (claimedVersion != null && !jenkins.getPlugin(name).isNewerThan(claimedVersion))) {
                        newPlugin.uploadTo(jenkins, injector, claimedVersion);
                    }
                } catch (IOException | ArtifactResolutionException e) {
                    throw new AssertionError("Failed to upload plugin: " + newPlugin, e);
                }
            }

            // TODO: Use better detection if this is actually necessary
            if (!isInstalled(shortNames)) {
                jenkins.restart();
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

                    sleep(1000);

                    try {
                        new UpdateCenter(jenkins).waitForInstallationToComplete(n);
                    } catch (InstallationFailedException e) {
                        if (e.getMessage().contains("Failed to download from")) {
                            continue;   // retry
                        }
                    }

                    continue OUTER;  // installation completed
                }
            }
        }
    }

    /**
     * Generates a map with shortNames and version.
     * Version is null if not declared.
     *
     * @param wpValues Values of the WithPlugin annotation
     * @return Map with Key:shortName Value:Version
     */
    private Map<String, String> getMapShortNamesVersion(String... wpValues) {
        Map<String, String> shortNamesVersion = new HashMap<>();
        for (String wpValue : wpValues) {
            PluginCoordinates coord = new PluginCoordinates(wpValue);
            shortNamesVersion.put(coord.getName(), coord.getVersion());
        }
        return shortNamesVersion;
    }

    public static class PluginCoordinates {
        private final @Nonnull String name;
        private final String version;
        public PluginCoordinates(String coordinates) {
            Iterator<String> spliter = Splitter.on("@").split(coordinates).iterator();
            name = spliter.next();
            version = spliter.hasNext()
                    ? spliter.next()
                    : null
            ;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }
}
