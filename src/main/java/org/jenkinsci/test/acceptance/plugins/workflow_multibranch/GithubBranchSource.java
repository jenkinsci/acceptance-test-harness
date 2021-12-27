package org.jenkinsci.test.acceptance.plugins.workflow_multibranch;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowSharedLibrary;
import org.openqa.selenium.support.ui.Select;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Base type for {@link PageAreaImpl} for Github Branch Source.
 */
@Describable("GitHub")
public class GithubBranchSource extends BranchSource {

    public final Control owner = control("repoOwner");
    public final Control repository = control("repository");
    public final Control credential = control("credentialsId" /* >= 2.2.0 */, "scanCredentialsId");

    public GithubBranchSource(WorkflowMultiBranchJob job, String path) {
        super(job, path);
    }

    public GithubBranchSource(WorkflowSharedLibrary sharedLibrary, String path) {
        super(sharedLibrary, path);
    }

    public GithubBranchSource owner(final String owner) {
        this.owner.set(owner);

        // This hack is necessary for the repository drop down population to be triggered
        final String elementTag = this.owner.resolve().getTagName();
        final String elementNameAttribute = this.owner.resolve().getAttribute("name");
        executeScript("var event = new Event('change');document.getElementsBySelector(\"" + elementTag + "[name='" + elementNameAttribute + "']\")[0].dispatchEvent(event);");
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

    /* As of GHBS 2.5.5 */
    public GithubBranchSource repoUrl(String url) {
        control("configuredByUrlRadio[true]").check();
        control(by.checkbox("Repository HTTPS URL")).check();
        return this;
    }
}
