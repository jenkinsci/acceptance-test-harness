import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Invoke Ant")
public class AntBuildStep extends BuildStep {
    public AntBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public void setTarget(String t) {
        control("targets").sendKeys(t);
    }

    public void setVersion(String t) {
        control("antName").sendKeys(t);
    }
}
