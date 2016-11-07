package org.jenkinsci.test.acceptance.plugins.artifactory;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Page Object for the Artifactory Publisher.
 * @author Eli Givoni
 */
@Describable("Deploy artifacts to Artifactory")
public class ArtifactoryPublisher extends AbstractStep implements PostBuildStep {

    public final Control artifactoryServer = control("");
    public final Control targetReleaseRepository = control("details/repositoryKey");
    public final Control targetSnapshotRepository = control("details/snapshotsRepositoryKey");
    public final Control customStagingConfiguration = control("details/userPluginKey");
    public final Control overrideDeployerCredentials = control("overridingDeployerCredentials");
    public final Control deployMavenArtifacts = control("deployArtifacts");
    public final Control propertiesDeployment = control("matrixParams");
    public final Control deployBuildInfo = control("deployBuildInfo");

    private static final String DEFAULT_REPO = "ext-release-local";

    public ArtifactoryPublisher(Job job, String path) {
        super(job, path);
    }

    public void refresh() {
        control("details/validate-button").click();
        waitFor(hasContent("Items refreshed successfully"));

        control("details/deployReleaseRepository/validate-button").click();
        control("details/deployReleaseRepository/keyFromText").sendKeys(DEFAULT_REPO);
        control("details/deploySnapshotRepository/validate-button").click();
        control("details/deploySnapshotRepository/keyFromText").sendKeys(DEFAULT_REPO);

    }
}
