package org.jenkinsci.test.acceptance.plugins.jacoco;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Page Object for the JacocoPublisher.
 */
@Describable("Record JaCoCo coverage report")
public class JacocoPublisher extends AbstractStep implements PostBuildStep {

    //    public final Control execPattern = control("execPattern");
    //    public final Control classPattern = control("classPattern");
    //    public final Control sourcePattern = control("sourcePattern");
    //    public final Control inclusionPattern = control("inclusionPattern");
    //    public final Control exclusionPattern = control("exclusionPattern");
    //    public final Control maximumInstructionCoverage = control("maximumInstructionCoverage");
    //    public final Control maximumBranchCoverage = control("maximumBranchCoverage");
    //    public final Control maximumComplexityCoverage = control("maximumComplexityCoverage");
    //    public final Control maximumLineCoverage = control("maximumLineCoverage");
    //    public final Control maximumMethodCoverage = control("maximumMethodCoverage");
    //    public final Control maximumClassCoverage = control("maximumClassCoverage");
    //    public final Control minimumInstructionCoverage = control("minimumInstructionCoverage");
    //    public final Control minimumBranchCoverage = control("minimumBranchCoverage");
    //    public final Control minimumComplexityCoverage = control("minimumComplexityCoverage");
    //    public final Control minimumLineCoverage = control("minimumLineCoverage");
    //    public final Control minimumMethodCoverage = control("minimumMethodCoverage");
    //    public final Control minimummaximumClassCoverage = control("minimumClassCoverage");
    public final Control changeBuildStatus = control("changeBuildStatus");

    public JacocoPublisher(Job job, String path) {
        super(job, path);
    }
}
