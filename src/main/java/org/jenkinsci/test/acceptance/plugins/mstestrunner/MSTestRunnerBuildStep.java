package org.jenkinsci.test.acceptance.plugins.mstestrunner;


import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

@Describable("Run unit tests with MSTest")
public class MSTestRunnerBuildStep extends AbstractStep implements BuildStep  {

    private final Control testFilesControl = control("testFiles");
    private final Control categoriesControl = control("categories");
    private final Control resultFileControl = control("resultFile");
    private final Control cmdLineArgsControl = control("cmdLineArgs");
    private final Control continueOnFailControl = control("continueOnFail");
    
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
