package org.jenkinsci.test.acceptance.msbuild;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

@Describable("Build a Visual Studio project or solution using MSBuild")
public class MSBuildStep extends AbstractStep implements BuildStep {

    private final Control msBuildName = control("msBuildName");
    private final Control msBuildFile = control("msBuildFile");
    private final Control cmdLineArgs = control("cmdLineArgs");

    public MSBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public MSBuildStep setMSBuildName(String buildName) {
        msBuildName.select(buildName);
        return this;
    }

    public MSBuildStep setMSBuildFile(String buildFile) {
        msBuildFile.set(buildFile);
        return this;
    }

    public MSBuildStep setCmdLineArgs(String cmdArgs) {
        cmdLineArgs.set(cmdArgs);
        return this;
    }
}
