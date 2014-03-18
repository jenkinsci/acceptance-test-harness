package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Encapsulates a model in Jenkins and wraps interactions with it.
 *
 * See https://code.google.com/p/selenium/wiki/PageObjects
 *
 * <p>
 * Most non-trivial page objects should derive from {@link ContainerPageObject}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject extends CapybaraPortingLayer {
    @Inject
    protected ObjectMapper jsonParser;

    /**
     * Full URL of the object that this page object represents. Ends with '/',
     * like "http://localhsot:8080/job/foo/"
     *
     * @see ContainerPageObject#url(String)
     *      Method that lets you resolve relative paths easily.
     */
    public final URL url;

    private static final AtomicLong IOTA = new AtomicLong(System.currentTimeMillis());

    public PageObject(Injector injector, URL url) {
        super(injector);
        this.url = url;
    }

    public static String createRandomName() {
        return "rand_name_"+IOTA.incrementAndGet();
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
            return new URL(url,rel);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public URL url(String format, Object... args) {
        return url(String.format(format,args));
    }
}
