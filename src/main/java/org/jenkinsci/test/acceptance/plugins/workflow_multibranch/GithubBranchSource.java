package org.jenkinsci.test.acceptance.plugins.workflow_multibranch;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Base type for {@link PageAreaImpl} for Github Branch Source.
 */
@Describable("GitHub")
public class GithubBranchSource extends BranchSource {

    public final Control owner = control("source/repoOwner");
    public final Control repository = control("source/repository");
    public final Control credential = control("source/scanCredentialsId");

    public GithubBranchSource(WorkflowMultiBranchJob job, String path) {
        super(job, path);
    }

    public GithubBranchSource owner(final String owner) {
        this.owner.set(owner);
        this.owner.sendKeys("" + Keys.TAB);
        return this;
    }

    public GithubBranchSource credential(final String credName) {
        this.credential.select(credName);
        return this;
    }

    public GithubBranchSource selectRepository(final String repoName) {
        waitFor().withTimeout(10, TimeUnit.SECONDS)
                .until(new Callable<Object>() {
                    @Override
                    public Boolean call() throws Exception {
                        final Select select = new Select(repository.resolve());
                        return select.getOptions().size() > 0;
                    }
                });


        this.repository.select(repoName);
        return this;
    }

}