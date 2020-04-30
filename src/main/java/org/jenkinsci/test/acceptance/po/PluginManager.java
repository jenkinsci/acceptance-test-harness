package org.jenkinsci.test.acceptance.po;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadataProvider;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.junit.internal.AssumptionViolatedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;

import hudson.util.VersionNumber;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.test.acceptance.update_center.MockUpdateCenter;


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
    private UpdateCenterMetadataProvider ucmd;

    /**
     * Optional configuration value that selects whether to resolve plugins locally and upload to Jenkins
     * (better performing when Jenkins is closer to the test execution), or install plugins from within Jenkins
     * (more accurate testing.)
     * @deprecated Blocks use of {@link MockUpdateCenter}.
     */
    @Inject(optional = true)
    @Named("uploadPlugins")
    @Deprecated
    public boolean uploadPlugins;

    @Inject(optional = true)
    @Named("forceRestartAfterPluginInstallation")
    public boolean forceRestart;

    @Inject
    public MockUpdateCenter mockUpdateCenter;

    public PluginManager(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("pluginManager/"));
        this.jenkins = jenkins;
    }

    /**
     * Force update the plugin update center metadata.
     */
    public void checkForUpdates() {
        mockUpdateCenter.ensureRunning();
        visit("advanced");
        final String current = getCurrentUrl();
        // The check now button is a form submit (POST) with a redirect to the same page only if the check is successful.
        // We use the button itself to detect when the page has changed, which happens after the refresh has been done
        // And we check for the presence of the button again
        clickButton("Check now");
        // The wait criteria is: we have left the current page and returned to the same one
        waitFor(find(by.button("Check now"))).withTimeout(30, TimeUnit.SECONDS).until(webElement -> {
                try {
                    // We interact with the element just to detect if it is stale
                    webElement.findElement(by.id("it does not matter"));
                } catch(StaleElementReferenceException e) {
                    // with this exception we know we've left the original page
                    // we look for an element in the page to check for success
                    if (current.equals(getCurrentUrl())) {
                        return true;
                    }
                } catch (Exception e) { // Any other exception means no successful check
                    return false;
                }
                // This should never happen but the closure needs to return
                return false;
        });
        updated = true;
    }

    public enum InstallationStatus {NOT_INSTALLED, OUTDATED, UP_TO_DATE}

    public InstallationStatus installationStatus(String spec) {
        return installationStatus(new PluginSpec(spec));
    }

    /**
     * @return whether the plugin (in version greater or equal than specified) is installed
     */
    public InstallationStatus installationStatus(PluginSpec spec) {
        String name = spec.getName();
        String version = spec.getVersion();
        Plugin plugin;
        try {
            plugin = jenkins.getPlugin(name);
            if (version != null) {
                VersionNumber actualVersion = plugin.getVersion();
                // check if installed version >= required version
                if (actualVersion.compareTo(new VersionNumber(version)) < 0) {
                    LOGGER.info(name + " has version " + actualVersion + " but " + version + " was requested");
                    return InstallationStatus.OUTDATED;
                }
            }
            return InstallationStatus.UP_TO_DATE;
        } catch (IllegalArgumentException ex) {
            return InstallationStatus.NOT_INSTALLED;
        }
    }

    @Deprecated
    public boolean isInstalled(String... specs) {
        for (String s : specs) {
            if (installationStatus(s) != InstallationStatus.UP_TO_DATE) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param specs plugin ids with optional version (e.g. "ldap" or "ldap@1.8")
     * @return true, if plugin (in version greater or equal than specified) is installed
     */
    public boolean isInstalled(PluginSpec... specs) {
        for (PluginSpec s : specs) {
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
     * @return Always false.
     */
    @Deprecated
    public boolean installPlugins(final PluginSpec... specs) throws UnableToResolveDependencies, IOException {
        final Map<String, String> candidates = getMapShortNamesVersion(specs);

        if (!updated) {
            checkForUpdates();
        }

        if (uploadPlugins) {
            LOGGER.warning("Installing plugins by direct upload. Better to use the default MockUpdateCenter.");
            // First check to see whether we need to do anything.
            // If not, do not consider transitive dependencies of the requested plugins,
            // which might force updates (and thus restarts) even though we already have
            // a sufficiently new version of the requested plugin.
            boolean someChangeRequired = false;
            for (PluginSpec spec : specs) {
                if (installationStatus(spec) != InstallationStatus.UP_TO_DATE) {
                    someChangeRequired = true;
                    break;
                }
            }
            if (!someChangeRequired) {
                return false;
            }
            List<PluginMetadata> pluginToBeInstalled = ucmd.get(jenkins).transitiveDependenciesOf(jenkins, Arrays.asList(specs));
            for (PluginMetadata newPlugin: pluginToBeInstalled) {
                final String name = newPlugin.getName();
                String requiredVersion = candidates.get(name);
                String availableVersion = newPlugin.getVersion();
                if (requiredVersion == null) { // a dependency
                    requiredVersion = availableVersion;
                }
                final String currentSpec = StringUtils.isNotEmpty(requiredVersion)
                    ? name + "@" + requiredVersion
                    : name
                ;
                InstallationStatus status = installationStatus(currentSpec);
                if (status != InstallationStatus.UP_TO_DATE) {
                    if (new VersionNumber(requiredVersion).compareTo(new VersionNumber(availableVersion)) > 0) {
                        throw new AssumptionViolatedException(
                                name + " has version " + availableVersion + " but " + requiredVersion + " was requested");
                    }
                    try {
                        newPlugin.uploadTo(jenkins, injector, availableVersion);
                    } catch (ArtifactResolutionException x) {
                        throw new UnableToResolveDependencies(x);
                    }
                }
            }
        } else {

            // JENKINS-50790 It seems that this page takes too much time to load when running in the new ci.jenkins.io
            try {
                driver.manage().timeouts().pageLoadTimeout(time.seconds(240), TimeUnit.MILLISECONDS);
                visit("available");
            } finally {
                driver.manage().timeouts().pageLoadTimeout(time.seconds(FallbackConfig.PAGE_LOAD_TIMEOUT), TimeUnit.MILLISECONDS);
            }
            // End JENKINS-50790

            final ArrayList<PluginSpec> update = new ArrayList<>();
            for (final PluginSpec n : specs) {
                switch (installationStatus(n)) {
                    case NOT_INSTALLED:
                        tickPluginToInstall(n);
                    break;
                    case OUTDATED:
                        update.add(n);
                    break;
                    case UP_TO_DATE:
                        // Nothing to do
                    break;
                    default: assert false: "Unreachable";
                }
            }

            clickButton("Install");

            // Plugins that are already installed in older version will be updated
            System.out.println("Plugins to be updated: " + update);
            if (!update.isEmpty()) {
                visit(""); // Updates tab
                driver.navigate().refresh();
                for (PluginSpec n : update) {
                    tickPluginToInstall(n);
                }
                clickButton("Download now and install after restart");
            }
        }

        // Jenkins will be restarted if necessary
        new UpdateCenter(jenkins).waitForInstallationToComplete(specs);

        return false;
    }

    private void tickPluginToInstall(PluginSpec spec) {
        String name = spec.getName();
        check(waitFor(by.xpath("//input[starts-with(@name,'plugin.%s.')]", name), 30));
        final VersionNumber requiredVersion = spec.getVersionNumber();
        if (requiredVersion != null) {
            final VersionNumber availableVersion = getAvailableVersionForPlugin(name);
            if (availableVersion.isOlderThan(requiredVersion)) {
                throw new AssumptionViolatedException(String.format(
                        "Version '%s' of '%s' is required, but available version is '%s'",
                        requiredVersion, name, availableVersion
                ));
            }
        }
    }

    private VersionNumber getAvailableVersionForPlugin(String pluginName) {
        // assuming we are on 'available' or 'updates' page
        String v = find(by.xpath("//input[starts-with(@name,'plugin.%s.')]/../../td[3]", pluginName)).getText();
        return new VersionNumber(v);
    }

    /**
     * Installs a plugin by uploading the *.jpi image.
     * @deprecated Not used when running {@link MockUpdateCenter}.
     */
    @Deprecated
    public void installPlugin(File localFile) throws IOException {
        try (CloseableHttpClient httpclient = new DefaultHttpClient()) {

            HttpPost post = new HttpPost(jenkins.url("pluginManager/uploadPlugin").toExternalForm());
            HttpEntity e = MultipartEntityBuilder.create()
                    .addBinaryBody("name", localFile, APPLICATION_OCTET_STREAM, "x.jpi")
                    .build();
            post.setEntity(e);
    
            HttpResponse response = httpclient.execute(post);
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new IOException("Failed to upload plugin: " + response.getStatusLine() + "\n" +
                        IOUtils.toString(response.getEntity().getContent()));
            } else {
                System.out.format("Plugin %s installed\n", localFile);
            }
        }
    }

    /**
     * Generates a map with shortNames and version.
     * Version is null if not declared.
     *
     * @param specs Values of the {@link WithPlugins} annotation
     * @return Map with Key:shortName Value:Version
     */
    private Map<String, String> getMapShortNamesVersion(PluginSpec... specs) {
        Map<String, String> shortNamesVersion = new HashMap<>();
        for (PluginSpec s : specs) {
            shortNamesVersion.put(s.getName(), s.getVersion());
        }
        return shortNamesVersion;
    }

    /**
     * Enable oder disable the specified plugin.
     * @param pluginName plugin id (e.g. "ldap")
     * @param state enable plugin if true, disable plugin if false
     */
    public void enablePlugin(String pluginName, boolean state) {
        visit("installed");
        check(find(by.url("plugin/" + pluginName)), state);
    }
}
