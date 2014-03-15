package org.jenkinsci.test.acceptance.plugins.ant;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import static java.util.Arrays.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class AntGlobalConfig extends PageArea {
    @Inject
    JenkinsController controller;

    public final Control name = control("tool/name");
    public final Control home = control("tool/home");

    @Inject
    public AntGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-tasks-Ant$AntInstallation");
    }

    public void addAutoInstallation(String name, String version) {
        this.name.set(name);
        // by default Install automatically is checked
        control("tool/properties/hudson-tools-InstallSourceProperty/installers/id").set(version);
        // TODO: whereas we expect a drop-down, we are seeing a text box (indicative of master not getting tool installer updates?)
//            .findElement(by.option(version)).click();
    }

    public void addLocalInstallation(String name, String antHome) {
        this.name.set(name);
        // by default Install automatically is checked - need to uncheck
        control("tool/properties/hudson-tools-InstallSourceProperty").uncheck();
        home.set(antHome);
    }

    /**
     * Creates a dummy Ant installation that forwards to another real one.
     * Mainly useful to verify that Jenkins is actually calling a specific Ant installation,
     * as opposed to the one that is on PATH.
     */
    public void addFakeInstallation(String name, String path) {
        try {
            // where is the real ant?
            // FIXME: this test assumes that Ant on local machine is the same location with Ant on remote machine
            String real = Iterables.find(asList(System.getenv("PATH").split(":")), new Predicate<String>() {
                public boolean apply(String p) {
                    return new File(p, "ant").exists();
                }
            });

            new File(path,"bin").mkdirs();
            File bin = new File(path,"bin/ant");
            FileUtils.writeStringToFile(bin, String.format(IOUtils.toString(getClass().getResource("fake-ant.sh")), real));
            Files.setPosixFilePermissions(bin.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));

            addLocalInstallation(name,path);
        } catch (IOException e) {
            throw new AssertionError("Failed to install fake ant at "+path,e);
        }
    }


    public void prepareAutoInstall() {
        // TODO: the ruby implementation assumes the test harness and master runs on the same system,
        // which is not always true.
    }
}
