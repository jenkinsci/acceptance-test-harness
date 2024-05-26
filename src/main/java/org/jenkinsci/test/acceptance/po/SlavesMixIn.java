package org.jenkinsci.test.acceptance.po;

import java.io.File;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.openqa.selenium.NoSuchElementException;

/**
 * Mix-in for slaves.
 *
 * @author Kohsuke Kawaguchi
 */
public class SlavesMixIn extends MixIn {
    private final Jenkins jenkins;

    public SlavesMixIn(Jenkins jenkins) {
        super(jenkins);
        this.jenkins = jenkins;
    }

    public <S extends Slave> S get(Class<S> type, String name) {
        return  newInstance(type,jenkins,name);
    }

    public <S extends Slave> S create(Class<S> type) {
        return create(type,createRandomName());
    }

    /**
     * Creates a new slave of the given type. After the call, the web driver is on the config page.
     */
    public <S extends Slave> S create(Class<S> type, String name) {
        visit("computer/new");

        find(by.input("name")).sendKeys(name);
        find(by.radioButton("Permanent Agent")).click();
        try {
            clickButton("Create");
        } catch (NoSuchElementException e) {
            clickButton("OK");
        }
        // This form submission will drop us on the configure page

        S s = newInstance(type, jenkins, name);
        // Automatic disabling of sticky elements doesn't occur after a redirect
        // so force it after the configuration page has loaded
        new Scroller(driver).disableStickyElements();


        // reasonable starting point values
        s.setExecutors(1);
        s.setRemoteFs(remoteFs(name));

        return s;
    }

    private String remoteFs(String name) {
        String base = System.getProperty("java.io.tmpdir");
        if (System.getenv("SLAVE_FS_BASE") != null) {
            base = System.getenv("SLAVE_FS_BASE");
        }
        if (!(base.endsWith("\\") || base.endsWith("/"))) {
            base += File.separatorChar;
        }

        return base + name;
    }
}
