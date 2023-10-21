/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * A container that stores nested items like {@link Job jobs}. A folder may contain a hierarchy of folders.
 */
@Describable("com.cloudbees.hudson.plugins.folder.Folder")
public class Folder extends TopLevelItem implements Container {

    private final JobsMixIn jobs;
    private final ViewsMixIn views;
    private final Control properties = control("/com-cloudbees-hudson-plugins-folder-properties-EnvVarsFolderProperty/properties");
    private final By viewTab = by.css(".tabBar .tab a");
    private final By activeViewTab = by.css(".tabBar .tab.active a");

    public Folder(final Injector injector, final URL url, final String name) {
        super(injector, url, name);

        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
    }

    public Folder(PageObject context, URL url, String name) {
        super(context, url, name);
        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
    }

    @Override
    public JobsMixIn getJobs() {
        return jobs;
    }

    @Override
    public ViewsMixIn getViews() {
        return views;
    }

    @Override
    public void delete() {
        open();
        runThenHandleDialog(() -> clickLink("Delete Folder"));
    }

    public void setEnvironmentalVariables(final Map<String, String> envVbles) {
        StringBuilder envVblesAsString = new StringBuilder();

        if (envVbles != null) {
            for (Map.Entry<String, String> envVble : envVbles.entrySet()) {
                envVblesAsString.append(String.format("%s=%s\n", envVble.getKey(), envVble.getValue()));
            }
        }

        this.properties.set(envVblesAsString.toString());
    }

    public List<String> getViewsNames() {
        final List<WebElement> viewTabs = this.getViewTabs();

        final List<String> viewNames = new LinkedList<>();
        for (final WebElement tab : viewTabs) {
            viewNames.add(tab.getText());
        }

        return viewNames;
    }

    public String getActiveViewName() {
        return find(activeViewTab).getText();
    }

    public <T extends View> T  selectView(final Class<T> type, final String viewName) {
        final List<WebElement> viewTabs = this.getViewTabs();
        int i = 0;

        for (final WebElement tab : viewTabs) {
            if (tab.getText().equalsIgnoreCase(viewName)) {
                tab.click();

                if (i == 0) {
                    // First tab redirects to Folder's home page
                    return null;
                } else {
                    return newInstance(type, injector, url("view/%s/", viewName));
                }
            }

            i++;
        }

        throw new NoSuchElementException(String.format("There is no view with name [%s]", viewName));
    }

    private List<WebElement> getViewTabs() {
        final List<WebElement> viewTabs = driver.findElements(viewTab);

        final int lastViewIndex = viewTabs.size() - 1;

        if ("+".equals(viewTabs.get(lastViewIndex).getText()) || "New View".equals(viewTabs.get(lastViewIndex).getAttribute("aria-label"))) {
            viewTabs.remove(lastViewIndex);
        }

        return viewTabs;
    }

}
