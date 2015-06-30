package org.jenkinsci.test.acceptance.po;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

import org.kohsuke.randname.RandomNameGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

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

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), url);
    }
}
