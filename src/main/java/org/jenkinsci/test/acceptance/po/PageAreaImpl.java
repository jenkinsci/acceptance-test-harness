package org.jenkinsci.test.acceptance.po;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 * Special kind of page object that maps to a portion of a page with multiple INPUT controls.
 * <p>
 * Typically we use this to map a set of controls in the configuration page, which is generated
 * by composing various config.jelly files from different extension points.
 *
 * @author Oliver Gondza
 */
public abstract class PageAreaImpl extends CapybaraPortingLayerImpl implements PageArea {
    /**
     * Element path that points to this page area.
     */
    private final String path;

    private final PageObject page;

    /**
     * @param context Parent page object area is scoped to.
     * @param path Absolute path to the area.
     */
    protected PageAreaImpl(PageObject context, String path) {
        super(context.injector);
        this.path = path;
        this.page = context;
    }

    /**
     * @param area Parent area new area is scoped to.
     * @param path Absolute or relative path prefix, if absolute must be prefixed
     * by {@code area.getPath()}. IOW, it needs to be <i>in</i> parent area.
     */
    protected PageAreaImpl(PageArea area, String path) {
        this(area.getPage(), path.startsWith(area.getPath())
                ? path
                : area.getPath() + "/" + path
        );

        if (path.startsWith("/") && !path.startsWith(area.getPath())) {
            throw new IllegalArgumentException(
                    "Child area '" + path + "' is not part of its parent: " + area.getPath()
            );
        }
    }

    @Override
    public WebElement self() {
        return find(path(""));
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getPath(String rel) {
        if (rel.length() == 0) {
            return path;
        }

        // this allows path("") and path("/") to both work
        if (rel.startsWith("/")) {
            rel = rel.substring(1);
        }
        return path + '/' + rel;
    }

    @Override
    public String getPath(String rel, int index) {
        assert index >= 0: "Negative index is forbidden";
        String path = getPath(rel);
        if (index == 0) return path;

        return path + '[' + index + ']';
    }

    @Override
    public PageObject getPage() {
        return page;
    }

    /**
     * Returns the "path" selector that finds an element by following the form-element-path plugin.
     * <p>
     * <a href="https://plugins.jenkins.io/form-element-path/">form-element-path-plugin</a>
     */
    @Override
    public By path(String rel) {
        return by.path(getPath(rel));
    }

    /**
     * Create a control object that wraps access to the specific INPUT element in this page area.
     * <p>
     * The {@link Control} object itself can be created early as the actual element resolution happens
     * lazily. This means {@link PageArea} implementations can put these in their fields.
     * <p>
     * Several paths can be provided to find the first matching element. Useful
     * when element path changed between versions.
     */
    @Override
    public Control control(String... relativePaths) {
        return new Control(this, relativePaths);
    }

    @Override
    public Control control(By selector) {
        return new Control(injector, selector);
    }

    public @NonNull String createPageArea(String name, Runnable action) throws TimeoutException {
        String pathPrefix = getPath() + '/' + name;
        return getPage().createPageArea(pathPrefix, action);
    }
}
