package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.openqa.selenium.NoSuchElementException;

/**
 * Page objecct for plugin manager.
 *
 * @author Kohsuke Kawaguchi
 */
public class PluginManager extends ContainerPageObject {
    /**
     * Did we fetch the update center metadata?
     */
    private boolean updated;

    public PluginManager(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("pluginManager/"));
    }

    /**
     * Force update the plugin update center metadata.
     */
    public void checkForUpdates() {
        visit("checkUpdates");
        waitFor(by.xpath("//span[@id='completionMarker' and text()='Done']"));
        updated = true;
        // This is totally arbitrary, it seems that the Available page doesn't
        // update properly if you don't sleep a bit
        sleep(5000);
    }

    public boolean isInstalled(String... shortNames) {
        visit("installed");
        sleep(100);
        try {
            for (String n : shortNames) {
                find(by.xpath("//input[@url='plugin/%s']", n));
            }
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
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

        if (!updated)
            checkForUpdates();

        visit("available");
        for (String n : shortNames) {
            check(find(by.xpath("//input[starts-with(@name,'plugin.%s.')]", n)));
        }
        clickButton("Install");
    }
}
