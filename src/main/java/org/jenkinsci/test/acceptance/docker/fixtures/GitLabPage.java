package org.jenkinsci.test.acceptance.docker.fixtures;

import com.google.inject.Injector;
import java.net.URL;
import org.jenkinsci.test.acceptance.po.PageObject;

/** Wait for GitLab landing page */
public class GitLabPage extends PageObject {
    public GitLabPage(Injector injector, URL url) {
        super(injector, url);
    }

    public boolean isReady() {
        var page = getPageSource();
        return page != null && page.contains("GitLab Community Edition");
    }
}
