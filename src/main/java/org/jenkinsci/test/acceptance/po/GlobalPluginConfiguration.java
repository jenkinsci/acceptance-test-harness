package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * PageArea generated from a server-side {@code GlobalPluginConfiguration} implementation.
 *
 * <p>
 * These page areas do not get fixed path name, so we need to figure that out from the hidden "name" field.
 * <p>
 * TODO: improve core to make this more robust
 *
 * @author Kohsuke Kawaguchi
 */
public class GlobalPluginConfiguration extends PageAreaImpl {
    public GlobalPluginConfiguration(JenkinsConfig context, String pluginShortName) {
        super(context, toPathName(context.driver, pluginShortName));
    }

    private static String toPathName(WebDriver d, String pluginShortName) {
        for (int i = 0; ; i++) {
            String path = "/jenkins-model-GlobalPluginConfiguration/plugin";
            if (i > 0) {
                path += String.format("[%d]", i);
            }

            WebElement e = d.findElement(by.path("%s/name", path));
            if (e.getAttribute("value").equals(pluginShortName)) {
                return path;
            }
        }
    }
}
