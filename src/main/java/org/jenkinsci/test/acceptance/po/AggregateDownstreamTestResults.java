package org.jenkinsci.test.acceptance.po;

@Describable("Aggregate downstream test results")
public class AggregateDownstreamTestResults extends AbstractStep implements PostBuildStep {

    public final Control specify = control("specify");
    public final Control jobs = control("specify/jobs");
    public final Control includeFailedBuilds = control("specify/includeFailedBuilds");

    public AggregateDownstreamTestResults(Job parent, String path) {
        super(parent, path);
    }
}
