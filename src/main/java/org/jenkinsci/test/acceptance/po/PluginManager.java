package org.jenkinsci.test.acceptance.po;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.junit.internal.AssumptionViolatedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

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
        final String current = getCurrentUrl();
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
                        if (current.equals(getCurrentUrl())) {
                            return true;
                        }
                    }
                } catch(Exception e) {
                }
                return true;
            }
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
    public boolean installPlugins(final PluginSpec... specs) throws UnableToResolveDependencies {
        final Map<String, String> candidates = getMapShortNamesVersion(specs);

        if (!updated) {
            checkForUpdates();
        }

        LOGGER.info("Installing plugins by direct upload: " + uploadPlugins);
        if (uploadPlugins) {
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
            List<PluginMetadata> pluginToBeInstalled = ucmd.get().transitiveDependenciesOf(jenkins, Arrays.asList(specs));
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
                    File localFile = newPlugin.resolve(injector, availableVersion);
                    installPlugin(localFile);
                }
            }
        } else {
            visit("available");

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
        check(find(by.xpath("//input[starts-with(@name,'plugin.%s.')]", name)));
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
    private Map<String, String> getMapShortNamesVersion(PluginSpec... specs) {
        Map<String, String> shortNamesVersion = new HashMap<>();
        for (PluginSpec s : specs) {
            shortNamesVersion.put(s.getName(), s.getVersion());
        }
        return shortNamesVersion;
    }
}
