package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Scm;

/**
 * Encapsulates the PageArea of the Subversion SCM
 *
 * @author Kohsuke Kawaguchi
 * @author Matthias Karl
 */
@Describable("Subversion")
public class SubversionScm extends Scm {
    public static final String ALWAYS_FRESH_COPY = "Always check out a fresh copy";
    public static final String CLEAN_CHECKOUT =
            "Emulate clean checkout by first deleting unversioned/ignored files, then 'svn update'";
    private static final String CHECK_OUT_STRATEGY = "Check-out Strategy";
    private static final String REPOSITORY_BROWSER = "Repository browser";

    public final Control url = control("locations/remote");
    public final Control btAdvanced = control(
            by.xpath(
                    "//td[table/tbody/tr/td[@class='setting-main']/input[@name='_.ignoreDirPropChanges']]/div[@class='advancedLink']//button"));
    public final Control local = control("locations/local");
    public final Control checkoutStrategy = control(by.xpath(
            "//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select | "
                    + "//div[contains(@class, 'setting-name') and normalize-space(text())='%s']/../div[@class='setting-main']/select | "
                    + "//div[contains(@class, 'jenkins-form-label') and normalize-space(text())='%s']/../div[@class='jenkins-select']/select",
            CHECK_OUT_STRATEGY, CHECK_OUT_STRATEGY, CHECK_OUT_STRATEGY));
    public final Control credentials = control("locations/credentialsId");
    public final Control repositoryBrowser = control(by.xpath(
            "//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select | "
                    + "//div[contains(@class, 'setting-name') and normalize-space(text())='%s']/../div[@class='setting-main']/select | "
                    + "//div[contains(@class, 'jenkins-form-label') and normalize-space(text())='%s']/../div[@class='jenkins-select']/select",
            REPOSITORY_BROWSER, REPOSITORY_BROWSER, REPOSITORY_BROWSER));

    public <T extends SvnRepositoryBrowser> T useRepositoryBrowser(final Class<T> type) {
        repositoryBrowser.selectDropdownMenuAlt(type);
        return this.newInstance(type, this, this.getPage().url(getPath("[1]")));
    }

    public SubversionSvmAdvanced advanced() {
        btAdvanced.click();
        return this.newInstance(SubversionSvmAdvanced.class, this.getPage(), this.getPage().url);
    }

    public SubversionScm(Job job, String path) {
        super(job, path);
    }
}
