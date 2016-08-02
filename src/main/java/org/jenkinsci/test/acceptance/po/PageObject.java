package org.jenkinsci.test.acceptance.po;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import org.kohsuke.randname.RandomNameGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.openqa.selenium.WebElement;

/**
 * Encapsulates a model in Jenkins and wraps interactions with it.
 * <p/>
 * See https://code.google.com/p/selenium/wiki/PageObjects
 * <p/>
 * <p/>
 * Most non-trivial page objects should derive from {@link ContainerPageObject}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject extends CapybaraPortingLayerImpl {
    @Inject
    protected ObjectMapper jsonParser;

    /**
     * Full URL of the object that this page object represents. Ends with '/', like "http://localhsot:8080/job/foo/"
     *
     * @see ContainerPageObject#url(String) Method that lets you resolve relative paths easily.
     */
    public final URL url;

    private static final RandomNameGenerator RND = new RandomNameGenerator();

    public PageObject(Injector injector, URL url) {
        super(injector);
        this.url = url;
    }

    protected PageObject(PageObject context, URL url) {
        this(context.injector, url);
    }

    public static String createRandomName() {
        return RND.next();
    }

    public Jenkins getJenkins() {
        // TODO try to find the real Jenkins root according to the owner of this object, via breadcrumb
        // Alternately, Job could have a method to get Jenkins by appending ../../ to its own URL, if not in a folder (need a separate method to find folder owner, but that needs its own page object too)
        return injector.getInstance(Jenkins.class);
    }

    /**
     * Visits the top page of this object.
     */
    public WebDriver open() {
        return visit(url);
    }

    /**
     * Given the path relative to {@link #url}, visit that page
     */
    public void visit(String relativePath) {
        visit(url(relativePath));
    }

    /**
     * Resolves relative path against {@link #url} and treats any exception a a fatal problem.
     */
    public URL url(String rel) {
        try {
            return new URL(url, rel);
        }
        catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public URL url(String format, Object... args) {
        return url(String.format(format, args));
    }

    /**
     * Create a control object that wraps access to the specific INPUT element in this page area.
     * <p/>
     * The {@link Control} object itself can be created early as the actual element resolution happens lazily. This
     * means {@link PageAreaImpl} implementations can put these in their fields.
     * <p/>
     * Several paths can be provided to find the first matching element. Useful when element path changed between
     * versions.
     */
    public Control control(String... relativePaths) {
        return new Control(this, relativePaths);
    }

    public Control control(By selector) {
        return new Control(injector, selector);
    }

    /**
     * Capture path attribute of newly created form chunk upon invoking action.
     *
     * Consider "Add" button in page area with path "/foo" that is supposed to create new page area with path "/foo/bar"
     * or "/foo/bar[n]". There are several problems with the straightforward approach:
     *  - Created area may or may not be the first one of its kind so figuring the "path" is nontrivial.
     *  - The area may can take a while to render so waiting is needed.
     *  - Even after the markup appears, it can take a while for "path" attribute is added.
     *
     * This method properly wait until the new path is known. To be used as:
     *
     *  String barPath = fooArea.createPageArea("/bar", () -> control("add-button").click());
     *  new FooBarArea(fooArea, barPath);
     *
     * @param pathPrefix Prefix of the expected path. The path is always absolute.
     * @param action An action that triggers the page area creation. Clicking the button, etc.
     * @return The surrounding path of the area, exception thrown when not able to find out.
     */
    public @Nonnull String createPageArea(final String pathPrefix, Runnable action) throws TimeoutException {
        assert pathPrefix.startsWith("/"): "Path not absolute";
        final By by = this.by.areaPath(pathPrefix);
        final int existing = all(by).size();
        action.run();

        return waitFor().withTimeout(10, TimeUnit.SECONDS).until(new Function<CapybaraPortingLayer, String>() {
            @Nullable @Override public String apply(@Nullable CapybaraPortingLayer input) {
                List<WebElement> current = all(by);
                int size = current.size();
                if (size == existing) return null; // Have not appeared yet
                if (size == existing + 1) { // Appeared
                    WebElement created = current.get(current.size() - 1);
                    return created.getAttribute("path");
                }

                throw new AssertionError(String.format("Number of elements was %d, is %d: %s", existing, size, current));
            }

            @Override public String toString() {
                return "Page area to appear: " + by;
            }
        });
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), url);
    }
}
