package org.jenkinsci.test.acceptance.po;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.update_center.PluginMetadata;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadataProvider;
import org.junit.AssumptionViolatedException;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;

import hudson.util.VersionNumber;

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
        mockUpdateCenter.ensureRunning(jenkins);
        visit("index");
        final String current = getCurrentUrl();
        // The check now button is a form submit (POST) with a redirect to the same page only if the check is successful.
        // We use the button itself to detect when the page has changed, which happens after the refresh has been done
        // And we check for the presence of the button again
        WebElement checkButton = find(by.link("Check now"));
        checkButton.click();
        // The wait criteria is: we have left the current page and returned to the same one
        waitFor(checkButton).withTimeout(java.time.Duration.of(time.seconds(30), ChronoUnit.MILLIS)).until(webElement -> {
            try {
                // We interact with the element just to detect if it is stale
                webElement.findElement(by.id("it does not matter"));
            } catch(StaleElementReferenceException | NoSuchElementException e) {
                // with this exception we know we've left the original page
                // we look for an element in the page to check for success
                if (current.equals(getCurrentUrl())) {
                    return true;
                }
            }
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
     * <p>
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
                for (PluginSpec n : update) {
                    tickPluginToInstall(n);
                }
                clickButton("Download now and install after restart");
            }
        }

        // Jenkins will be restarted if necessary
        boolean hasBeenRestarted = new UpdateCenter(jenkins).waitForInstallationToComplete(specs);
        if (!hasBeenRestarted && specs.length > 0 && jenkins.getVersion().isNewerThan(new VersionNumber("2.188"))) {
            jenkins.getLogger("all").waitForLogged(Pattern.compile("Completed installation of .*"), 1000);
        }

        return false;
    }

    private void tickPluginToInstall(PluginSpec spec) {
        String name = spec.getName();
        WebElement filterBox = find(By.id("filter-box"));
        filterBox.clear();
        filterBox.sendKeys(name);

        // the target plugin web element becomes stale due to the dynamic behaviour of the plugin
        // manager UI which ends up with StaleElementReferenceException.
        // This is re-trying until the element can be properly checked.
        waitFor().withTimeout(10, TimeUnit.SECONDS).until(() -> {
            try {
                check(find(by.xpath("//input[starts-with(@name,'plugin.%s.')]", name)));
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                return false;
            }
            return true;
        });

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

    public VersionNumber getAvailableVersionForPlugin(String pluginName) {
        // assuming we are on 'available' or 'updates' page
        WebElement filterBox = find(By.id("filter-box"));
        filterBox.clear();
        filterBox.sendKeys(pluginName);
        
        // DEV MEMO: There is a {@code data-plugin-version} attribute on the {@code tr} tag holding the plugin line entry in the
        // plugin manager. By selecting the line, we can then retrieve the version directly without having to parse the line's content.
        final String xpathToPluginLine = "//input[starts-with(@name,'plugin.%s.')]/ancestor::tr";

        // DEV MEMO: To avoid flakiness issues, wait for the text to be entirely written in the search box, and
        // wait for the list below to be properly refreshed and for the element we are searching for to be displayed.
        // If not, the list might not be properly refreshed and the element would never be found.
        waitFor().withTimeout(10, TimeUnit.SECONDS).until(() -> {
            try {
                check(find(by.xpath(xpathToPluginLine, pluginName)));
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                return false;
            }
            return true;
        });
        
        String version = find(by.xpath(xpathToPluginLine, pluginName)).getAttribute("data-plugin-version");
        return new VersionNumber(version);
    }

    /**
     * Installs a plugin by uploading the *.jpi image.
     * Can be use for corner cases to verify some behavior when very specific plugin versions are needed, for example
     * to verify that a warning message is displayed when a risky and outdated plugin is installed on Jenkins.
     *
     * @deprecated Not used when running {@link MockUpdateCenter}.
     */
    @Deprecated
    public void installPlugin(File localFile) throws IOException {
        jenkins.getPluginManager().visit("advanced");
        // the name of the input is actually 'name'
        final By xpath = by.xpath("//form//input[@type='file' and @name='name']");
        final WebElement fileUpload = waitFor(xpath);
        control(xpath).set(localFile.getAbsolutePath());
        // click will trigger this: https://github.com/jenkinsci/jenkins/blob/c5f996a2ee5aa53e47480ebefb5f47899cf1e471/core/src/main/java/hudson/PluginManager.java#L1799
        // at the end of the method, there is a redirection to '../updateCenter'
        // on this page, a table will display the status of uploaded plugins
        waitFor(by.button("Deploy")).click();

        // the table may contain many plugins, the shortName is used to locate the correct table row
        // this is stolen from: https://github.com/jenkinsci/jenkins/blob/c5f996a2ee5aa53e47480ebefb5f47899cf1e471/core/src/main/java/hudson/PluginManager.java#L1844-L1865
        String shortName;
        try (JarFile j = new JarFile(localFile)) {
            String name = j.getManifest().getMainAttributes().getValue("Short-Name");
            if (name != null) {
                shortName = name;
            } else {
                String baseName = FilenameUtils.getBaseName(localFile.getName());
                shortName = baseName.substring(0, baseName.lastIndexOf('-'));
            }
        }

        // First, wait for the table row to be loaded
        // this is mandatory because it ensures that the file is uploaded and allows the use of the "wait until" below
        WebElement pluginRow = waitFor(by.xpath("//*[@id='log']//*[descendant::*[normalize-space(text())='%1$s']]", shortName));
        // Then wait until the plugin is loaded
        waitFor()
                .withMessage("All plugins should be installed")
                .withTimeout(5, TimeUnit.MINUTES)
                .until(() -> {
                    List<WebElement> elements = pluginRow.findElements(by.xpath("descendant::*[contains(.,'Pending') or contains(.,'Installing')]"));
                    return elements.isEmpty();
                });
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
        WebElement filterBox = find(By.id("filter-box"));
        filterBox.clear();
        filterBox.sendKeys(pluginName);
        check(find(by.url("plugin/" + pluginName)), state);
    }
}
