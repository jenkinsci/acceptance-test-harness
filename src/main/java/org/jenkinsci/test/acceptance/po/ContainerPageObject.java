package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * {@link PageObject} that represents a model that has multiple views underneath.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ContainerPageObject extends ConfigurablePageObject {
    protected ContainerPageObject(Injector injector, URL url) {
        super(injector, url);
        if (!url.toExternalForm().endsWith("/")) {
            throw new IllegalArgumentException("URL should end with '/': " + url);
        }
    }

    @Override
    public URL getConfigUrl() {
        return url("configure");
    }

    protected ContainerPageObject(PageObject context, URL url) {
        super(context, url);
        if (!url.toExternalForm().endsWith("/")) {
            throw new IllegalArgumentException("URL should end with '/': " + url);
        }
    }

    public URL getJsonApiUrl() {
        return url("api/json");
    }

    /**
     * Makes the API call and obtains JSON representation.
     */
    public JsonNode getJson() {
        return getJson(null);
    }

    /**
     * @param queryString Additional query string to narrow down the data retrieval, like "tree=..." or "depth=..."
     */
    public JsonNode getJson(String queryString) {

        URL url = getJsonApiUrl();
        HttpURLConnection con = null;
        try {
            if (queryString != null) {
                url = new URL(url + "?" + queryString);
            }

            // Pass in all the cookies (in particular the session cookie.)
            // This ensures that the API call sees what the current user sees.
            con = IOUtil.openConnection(url);
            con.setRequestProperty("Cookie", StringUtils.join(driver.manage().getCookies(), ";"));
            return jsonParser.readTree(con.getInputStream());
        } catch (MalformedURLException e) {
            throw new Error(e);
        } catch (IOException e) {
            throw new NoSuchElementException("Failed to read from " + url, e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Create action of this page object.
     * See {@link Action}, {@link ActionPageObject}
     *
     * @param type Action type to create.
     */
    public <T extends Action> T action(Class<T> type) {
        final String path = type.getAnnotation(ActionPageObject.class).relativePath();
        final String linkText = type.getAnnotation(ActionPageObject.class).linkText();
        return action(type, path, linkText.isBlank() ? null : linkText);
    }

    /**
     * Create action of this page object.
     * Preffer to use {@link #action(Class)} where fixed paths and link text is known.
     * @param <T> The concrete action class to create.
     * @param type Action type to create
     * @param path the relative path from this PageObject
     * @param linkText optional text of the link in the sidebar.
     * @return the newly contructed Action of type T
     */
    public <T extends Action> T action(Class<T> type, String path, String linkText) {

        T instance = newInstance(type, this, path, linkText);

        if (!instance.isApplicable(this)) {
            throw new AssertionError(
                    "Action can not be attached to " + getClass().getCanonicalName());
        }

        return instance;
    }

    /**
     * Get a map with all links within the navigation area.
     * The key contains the href attribute while the value contains the link text.
     *
     * @return A map with all links within the navigation area.
     */
    public Map<String, String> getNavigationLinks() {
        open();
        final Map<String, String> links = new HashMap<>();
        List<WebElement> elementLinks = all(By.cssSelector("#tasks a.task-link"));

        for (WebElement element : elementLinks) {
            links.put(element.getAttribute("href"), element.getText());
        }
        return links;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }

        if (!(other instanceof ContainerPageObject rhs)) {
            return false;
        }

        return this.url.toExternalForm().equals(rhs.url.toExternalForm());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ url.hashCode();
    }
}
