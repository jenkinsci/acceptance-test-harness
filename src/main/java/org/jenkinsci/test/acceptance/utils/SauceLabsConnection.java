package org.jenkinsci.test.acceptance.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Encapsulates username and password for Sauce Labs.
 *
 * <p>
 * The credential is expected to be at {@code ~/.saucelabs}
 *
 * @author Kohsuke Kawaguchi
 */
public class SauceLabsConnection {
    private String username;
    private String password;

    public SauceLabsConnection() throws IOException {
        File conf = new File(System.getProperty("user.home"),".sauce-ondemand");
        if (!conf.exists())
            throw new IOException("SauceLabs connection file is missing: "+conf);

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(conf.toPath())) {
            props.load(is);
        }
        username = props.getProperty("username");
        password = props.getProperty("key");

        if (username==null || password==null)
            throw new IOException("Missing username/key entries in "+conf);
    }

    public WebDriver createWebDriver(Capabilities caps) throws IOException {
        return new RemoteWebDriver(new URL(
                String.format("http://%s:%s@ondemand.saucelabs.com/wd/hub",username,password)),caps);
    }
}
