package org.jenkinsci.test.acceptance.msbuild;

import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallationPageObject;

@ToolInstallationPageObject(installer = "", name = "MSBuild")
public class MSBuildInstallation extends ToolInstallation  {

    public MSBuildInstallation(JenkinsConfig context, String path) {
        super(context, path);
    }

}
