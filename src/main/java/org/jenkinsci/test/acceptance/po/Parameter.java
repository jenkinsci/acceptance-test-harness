package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;

/**
 * Parameter for builds.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Parameter extends PageArea {
    public final Job job;
    private String name;

    public Parameter(Job job, String path) {
        super(job.injector,path);
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        control("name").set(name);
    }

    public void setDescription(String v) {
        control("description").set(v);
    }

    /**
     * Given an subtype-specific value object that represents the actual argument,
     * fill in the parameterized build form.
     */
    public abstract void fillWith(Object v);

    /**
     * Depending on whether we are on the config page or in the build page, this is different.
     */
    @Override
    public By path(String rel) {
        if (driver.getCurrentUrl().endsWith("/configure"))
            return super.path(rel);

        String np = find(by.xpath("//input[@name='name' and @value='%s']", name)).getAttribute("path");
        String path = np.replaceAll("/name$", "") + "/" + rel;
        return by.path(path);
    }
}
