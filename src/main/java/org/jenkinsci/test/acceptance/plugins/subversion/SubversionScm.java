package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Scm;
import org.jenkinsci.test.acceptance.po.ScmPageObject;

/**
 * @author Kohsuke Kawaguchi
 */
@ScmPageObject("Subversion")
public class SubversionScm extends Scm {
    public final Control url = control("locations/remote");
    public final Control local = control("locations/local");

    public SubversionScm(Job job, String path) {
        super(job, path);
    }
}
