package org.jenkinsci.test.acceptance.plugins.msbuild;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallationPageObject;

@ToolInstallationPageObject(installer = "", name = "MSBuild")
public class MSBuildInstallation extends ToolInstallation {

    public MSBuildInstallation(Jenkins context, String path) {
        super(context, path);
    }
}
