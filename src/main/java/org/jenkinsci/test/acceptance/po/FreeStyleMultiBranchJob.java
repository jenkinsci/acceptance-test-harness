package org.jenkinsci.test.acceptance.po;

import java.net.URL;

import com.google.inject.Injector;

/**
 * A freestyle multi-branch job (requires installation of multi-branch-project-plugin).
 *
 * @author Ullrich Hafner
 */
@Describable("com.github.mjdetullio.jenkins.plugins.multibranch.FreeStyleMultiBranchProject")
public class FreeStyleMultiBranchJob extends Job {
    public FreeStyleMultiBranchJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }
}
