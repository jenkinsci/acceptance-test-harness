package org.jenkinsci.test.acceptance.po;

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
        try {
            for (String n : shortNames) {
                find(by.xpath("//input[@url='plugin/%s']", n));
            }
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

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
