package org.jenkinsci.test.acceptance.plugins.job_dsl;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;

import java.net.URL;

/**
 * Encapsulates the PageObject of the advanced area of the Job DSL plugin.
 *
 * @author Maximilian Oeckler
 */
public class JobDSLBuildStepAdvanced extends PageObject {
    private final Control lookupStrategy = control(by.input("_.lookupStrategy"));
    private final Control additionalClasspath = control(by.input("_.additionalClasspath"));
    public final Control btExpandClasspathArea = control(by.xpath("//tr[td/input[@id='textarea._.additionalClasspath' and @name='_.additionalClasspath']]//input[@type='button']"));

    public final Control failOnMissingPlugin = control(by.input("failOnMissingPlugin"));
    public final Control unstableOnDeprecation = control(by.input("unstableOnDeprecation"));

    public JobDSLBuildStepAdvanced(PageObject context, URL url) {
        super(context, url);
    }

    /**
     * Set the context to use for relative job names.
     * @param strategy The strategy to select. A Element of the type {@link JobDSLLookupStrategy}
     */
    public void setLookupStrategy(JobDSLLookupStrategy strategy) {
        this.lookupStrategy.select(strategy.toString());
    }

    /**
     * Newline separated list of additional classpath entries for the Job DSL scripts.
     * All entries must be relative to the workspace root.
     * @param classpaths The additional classpaths.
     */
    public void setAdditionalClasspath(final String... classpaths) {
        btExpandClasspathArea.click();
        this.additionalClasspath.set(StringUtils.join(classpaths, "\n"));
    }
}
