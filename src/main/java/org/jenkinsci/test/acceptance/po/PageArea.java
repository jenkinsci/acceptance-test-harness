package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;

/**
 * Special kind of page object that maps to a portion of a page with multiple INPUT controls.
 *
 * <p>
 * Typically we use this to map a set of controls in the configuration page, which is generated
 * by composing various config.jelly files from different extension points.
 *
 * @author Oliver Gondza
 */
public abstract class PageArea extends CapybaraPortingLayer implements Control.Owner {
    /**
     * Element path that points to this page area.
     */
    public final String path;

    public final PageObject page;

    protected PageArea(PageObject context, String path) {
        super(context.injector);
        this.path = path;
        this.page = context;
    }

    protected PageArea(PageArea area, String relativePath) {
        this(area.page, area.path + "/" + relativePath);

        if (relativePath.startsWith("/")) throw new IllegalArgumentException(
                "Path is supposed to be relative to page area. Given: " + relativePath
        );
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     *
     * https://wiki.jenkins-ci.org/display/JENKINS/Form+Element+Path+Plugin
     */
    @Override
    public By path(String rel) {
        if (rel.length()==0)    return by.path(path);

        // this allows path("") and path("/") to both work
        if (rel.startsWith("/"))    rel=rel.substring(1);
        return by.path(path + '/' + rel);
    }

    /**
     * Create a control object that wraps access to the specific INPUT element in this page area.
     *
     * The {@link Control} object itself can be created early as the actual element resolution happens
     * lazily. This means {@link PageArea} implementations can put these in their fields.
     *
     * Several paths can be provided to find the first matching element. Useful
     * when element path changed between versions.
     */
    public Control control(String... relativePaths) {
        return new Control(this,relativePaths);
    }

    public Control control(By selector) {
        return new Control(injector,selector);
    }
}
