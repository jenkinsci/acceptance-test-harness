package org.jenkinsci.test.acceptance.plugins.mstestrunner;


import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

@Describable("Run unit tests with MSTest")
public class MSTestRunnerBuildStep extends AbstractStep implements BuildStep  {

    public final Control testFilesControl = control("testFiles");
    public final Control categoriesControl = control("categories");
    public final Control resultFileControl = control("resultFile");
    public final Control cmdLineArgsControl = control("cmdLineArgs");
    public final Control continueOnFailControl = control("continueOnFail");
    
    public MSTestRunnerBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public void configure(String testFiles, String resultFile, String categories, String cmdLineArgs, boolean ignoreFailingTests) {
        this.resultFileControl.set(resultFile);
        this.testFilesControl.set(testFiles);
        this.continueOnFailControl.check(ignoreFailingTests);
        if (StringUtils.isNotBlank(categories)) {
            this.categoriesControl.set(categories);
        }
        if (StringUtils.isNotBlank(cmdLineArgs)) {
            this.cmdLineArgsControl.set(cmdLineArgs);
        }
    }

}
