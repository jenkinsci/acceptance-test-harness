package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;

/**
 * Parameter for builds.
 * <p>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Parameter extends PageAreaImpl {
    public final Job job;
    private String name;

    public Parameter(Job job, String path) {
        super(job, path);
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public Parameter setName(String name) {
        this.name = name;
        control("name").set(name);
        return this;
    }

    public Parameter setDescription(String v) {
        control("description").set(v);
        return this;
    }

    public Parameter setDefault(String value) {
        control("defaultValue").set(value);
        return this;
    }

    /**
     * Given an subtype-specific value object that represents the actual argument, fill in the parameterized build
     * form.
     */
    public abstract void fillWith(Object v);

    /**
     * Depending on whether we are on the config page or in the build page, this is different.
     */
    @Override
    public By path(String rel) {
        if (driver.getCurrentUrl().endsWith("/configure")) {
            return super.path(rel);
        }

        if (Boolean.getBoolean("new-job-page.flag.defaultValue")) {
            return by.xpath(
                    "//input[@name='name' and @value='%s']/parent::div[@name='parameter']//*[@name='%s']", name, rel);
        }

        String np = driver.findElement(by.xpath("//input[@name='name' and @value='%s']", name))
                .getAttribute("path");
        String path = np.replaceAll("/name$", "") + "/" + rel;
        return by.path(path);
    }
}
