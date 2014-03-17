package org.jenkinsci.test.acceptance.po;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.UpdateCenter.InstallationFailedException;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata;
import org.sonatype.aether.resolution.ArtifactResolutionException;

import javax.inject.Provider;

import static java.util.Arrays.asList;

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
    Provider<UpdateCenterMetadata> ucmd;

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
        do {
            sleep(5000);
        } while (!(l.hasLogged(ant) && l.hasLogged(maven) && l.hasLogged(jdk)));
    }

    public boolean isInstalled(String... shortNames) {
        visit("installed");
        for (String n : shortNames) {
            if (getElement(by.xpath("//input[@url='plugin/%s']", n))==null)
                return false;
        }
        return true;
    }

    /**
     * Installs specified plugins.
     *
     * @deprecated
     *      Please be encouraged to use {@link WithPlugins} annotations to statically declare
     *      the required plugins you need. If you really do need to install plugins in the middle
     *      of a test, as opposed to be in the beginning, then this is the right method.
     *
     *      The deprecation marker is to call attention to {@link WithPlugins}. This method
     *      is not really deprecated.
     */
    public void installPlugin(String... shortNames) {
        if (isInstalled(shortNames))
            return;

        if (UPLOAD) {
            for (PluginMetadata p : ucmd.get().transitiveDependenciesOf(asList(shortNames))) {
                try {
                    p.uploadTo(jenkins,injector);
                } catch (IOException|ArtifactResolutionException e) {
                    throw new AssertionError("Failed to upload plugin: "+p,e);
                }
            }
        } else {
            if (!updated)
                checkForUpdates();

            OUTER:
            for (final String n : shortNames) {
                for (int attempt=0; attempt<2; attempt++) {// # of installations attempted, considering retries
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

    // TODO: make this properly configurable
    private static boolean UPLOAD = Boolean.getBoolean("UPLOAD");
}
