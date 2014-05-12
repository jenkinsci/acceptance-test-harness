package org.jenkinsci.test.acceptance.po;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.UpdateCenter.InstallationFailedException;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata;

import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * Page object for plugin manager.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginManager extends ContainerPageObject {
    private static final String VERSION_SEPARATOR = "@";
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
        visit("installed");
        for (String n : shortNames) {
            String version = getVersionFromWpValue(n) != null ? getVersionFromWpValue(n) : ucmd.get().plugins.get(n).version;
            if (getElement(by.xpath("//input[@url='plugin/%s']/../../td/a[text()='%s']", getShortNameFromWpValue(n), version)) == null)
                return false;
        }
        return true;
    }

    private void waitForIsInstalled(final String... shortNames) {
        waitForCond(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isInstalled(shortNames);
            }
        }, 180);
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
    public void installPlugin(String... shortNames) {
        final Map<String, String> mapShortNamesVersion = getMapShortNamesVersion(shortNames);

        if (isInstalled(shortNames))
            return;
        if (uploadPlugins) {
            for (PluginMetadata p : ucmd.get().transitiveDependenciesOf(mapShortNamesVersion.keySet())) {
                try {
                    final String claimedVersion = mapShortNamesVersion.get(p.name) != null ? mapShortNamesVersion.get(p.name) : p.version;
                    if (!isInstalled(p.name + VERSION_SEPARATOR + claimedVersion)) {
                        p.uploadTo(jenkins, injector, claimedVersion);
                    }
                } catch (IOException | ArtifactResolutionException e) {
                    throw new AssertionError("Failed to upload plugin: " + p, e);
                }
            }
            //to enable updated plugins/corePlugins
            jenkins.restart();
            waitForIsInstalled(shortNames);
        } else {
            if (!updated)
                checkForUpdates();

            OUTER:
            for (final String n : mapShortNamesVersion.keySet()) {
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
     * Parses the version from a wpValue.
     * Versions can be added to the shortName via "@"
     * If no version is added, method returns null
     *
     * @param wpValue Value of the WithPlugin annotation
     * @return version or null
     */
    private String getVersionFromWpValue(String wpValue) {
        final Iterable<String> split = Splitter.on(VERSION_SEPARATOR).split(wpValue);
        if (Iterables.size(split) == 1) {
            return null;
        } else {
            return Iterables.getLast(split);
        }
    }

    /**
     * Parses the shortName from a wpValue.
     *
     * @param wpValue Value of the WithPlugin annotation
     * @return shortName
     */
    private String getShortNameFromWpValue(String wpValue) {
        final Iterable<String> split = Splitter.on(VERSION_SEPARATOR).split(wpValue);
        return split.iterator().next();
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
            shortNamesVersion.put(getShortNameFromWpValue(wpValue), getVersionFromWpValue(wpValue));
        }
        return shortNamesVersion;
    }

}
