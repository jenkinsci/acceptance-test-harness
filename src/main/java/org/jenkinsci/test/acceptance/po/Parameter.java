package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;

import java.util.LinkedList;
import java.util.List;

/**
 * Parameter for builds.
 * <p/>
 * Use {@link Describable} annotation to register an implementation. The value of {@link Describable} must be the
 * corresponding caption of the add parameter menu item AND the corresponding {@link hudson.model.ParameterDefinition}
 * class. For example {"String Parameter", "hudson.model.StringParameterDefinition"}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Parameter extends PageAreaImpl {
    private static List<Class<? extends Parameter>> subtypes = new LinkedList<>();

    public final Job job;
    private String name;

    public Parameter(Job job, String path) {
        super(job, path);
        this.job = job;
        if (!Parameter.subtypes.contains(this.getClass())) {
            Parameter.subtypes.add(this.getClass());
        }
    }

    public String getName() {
        return name;
    }

    public Parameter setName(String name) {
        this.name = name;
        control("name").set(name);
        return this;
    }

    /**
     * Sets this objects name property without modifying the UI
     */
    void setNameProperty(String name) {
        this.name = name;
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

        String np = driver.findElement(by.xpath("//input[@name='name' and @value='%s']", name)).getAttribute("path");
        String path = np.replaceAll("/name$", "") + "/" + rel;
        return by.path(path);
    }

    public static List<Class<? extends Parameter>> all() {
        return Parameter.subtypes;
    }
}
