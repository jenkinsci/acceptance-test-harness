package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import jakarta.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.kohsuke.randname.RandomNameGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Encapsulates a model in Jenkins and wraps interactions with it.
 * <p>
 * See <a href="https://code.google.com/p/selenium/wiki/PageObjects">...</a>
 * <p>
 * Most non-trivial page objects should derive from {@link ContainerPageObject}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject extends CapybaraPortingLayerImpl {
    @Inject
    protected ObjectMapper jsonParser;

    /**
     * Full URL of the object that this page object represents. Ends with '/', like "http://localhost:8080/job/foo/"
     *
     * @see ContainerPageObject#url(String) Method that lets you resolve relative paths easily.
     */
    public final URL url;

    /**
     * If the object was created with some context, preserve it so that we can
     * easily get the real Jenkins root
     */
    private PageObject context;

    private static final RandomNameGenerator RND = new RandomNameGenerator();

    /**
     * @deprecated Use {@link #PageObject(PageObject, URL)} instead to preserve context.
     *             This constructor should only be used for top-level objects like {@link Jenkins}.
     */
    @Deprecated
    public PageObject(Injector injector, URL url) {
        super(injector);
        this.url = url;
    }

    protected PageObject(PageObject context, URL url) {
        this(context.injector, url);
        this.context = context;
    }

    public static String createRandomName() {
        return RND.next();
    }

    public Jenkins getJenkins() {
        if (context != null) {
            return context.getJenkins();
        }
        // TODO try to find the real Jenkins root according to the owner of this object, via breadcrumb
        // Alternately, Job could have a method to get Jenkins by appending ../../ to its own URL, if not in a folder
        // (need a separate method to find folder owner, but that needs its own page object too)
        return injector.getInstance(Jenkins.class);
    }

    /**
     * Visits the top page of this object.
     */
    public WebDriver open() {
        return visit(url);
    }

    /**
     * Ensures that the current page is the one represented by this PageObject, and opens it if not.
     */
    protected void ensureOpen() {
        if (!url.toExternalForm().equals(driver.getCurrentUrl())) {
            open();
        }
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
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public URL url(String format, Object... args) {
        return url(String.format(format, args));
    }

    /**
     * Create a control object that wraps access to the specific INPUT element in this page area.
     * <p>
     * The {@link Control} object itself can be created early as the actual element resolution happens lazily. This
     * means {@link PageAreaImpl} implementations can put these in their fields.
     * <p>
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
     * <p>
     * Consider "Add" button in page area with path "/foo" that is supposed to create new page area with path "/foo/bar"
     * or "/foo/bar[n]". There are several problems with the straightforward approach:
     *  - Created area may or may not be the first one of its kind so figuring the "path" is nontrivial.
     *  - The area may can take a while to render so waiting is needed.
     *  - Even after the markup appears, it can take a while for "path" attribute is added.
     * <p>
     * This method properly wait until the new path is known. To be used as:
     *
     * <pre>
     * {@code
     *  String barPath = fooArea.createPageArea("/bar", () -> control("add-button").click());
     *  new FooBarArea(fooArea, barPath);
     *  }
     *  </pre>
     *
     * @param pathPrefix Prefix of the expected path. The path is always absolute.
     * @param action An action that triggers the page area creation. Clicking the button, etc.
     * @return The surrounding path of the area, exception thrown when not able to find out.
     */
    public @NonNull String createPageArea(final String pathPrefix, Runnable action) throws TimeoutException {
        assert pathPrefix.startsWith("/") : "Path not absolute: " + pathPrefix;
        final By by = PageObject.by.areaPath(pathPrefix);
        final List<String> existing = extractPaths(all(by));
        final int existingSize = existing.size();
        action.run();

        return waitFor().withTimeout(Duration.ofSeconds(10)).until(new Function<>() {
            @Nullable
            @Override
            public String apply(@Nullable CapybaraPortingLayer input) {
                List<String> current = extractPaths(all(by));
                int size = current.size();
                if (size == existingSize) {
                    return null; // Have not appeared yet
                }
                if (size == existingSize + 1) { // Appeared
                    current.removeAll(existing);
                    assert current.size() == 1 : "Path mismatch. Existing: " + existing + "; filtered: " + current;
                    return current.get(0);
                }

                throw new AssertionError(
                        String.format("Number of elements was %d, now is %d: %s", existingSize, size, current));
            }

            @Override
            public String toString() {
                return "Page area to appear: " + by;
            }
        });
    }

    private List<String> extractPaths(Collection<WebElement> elements) {
        List<String> paths = new ArrayList<>(elements.size());
        for (WebElement element : elements) {
            paths.add(element.getAttribute("path"));
        }
        return paths;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), url);
    }

    protected PageObject getContext() {
        return context;
    }
}
