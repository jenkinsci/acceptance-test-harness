package org.jenkinsci.test.acceptance.po;

import javax.annotation.CheckReturnValue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Injector;

/**
 * Super class for top level items. 
 * Top level items include {@link Job}s and other non-buildable items
 * such as {@link FolderItem}s.
 * Use {@link Describable} annotation to register an implementation.
 */
public abstract class TopLevelItem extends ContainerPageObject {
    public String name;

    public List<Parameter> getParameters() {
        return parameters;
    }

    private List<Parameter> parameters = new ArrayList<>();

    public TopLevelItem(Injector injector, URL url, String name) {
        super(injector, url);
        this.name = name;
    }

    public TopLevelItem(PageObject context, URL url, String name) {
        super(context, url);
        this.name = name;
    }

    /**
     * Renames the job. Opens the configuration section, sets the name and saves the form. Finally the rename is
     * confirmed.
     *
     * @param newName the new name of the job
     * @return the renamed job (with new URL)
     */
    @CheckReturnValue
    public <T extends TopLevelItem> T renameTo(final String newName) {
        configure();
        String oldName = name;
        control("/name").set(newName);
        save();
        clickButton("Yes");

        try {
            return (T) newInstance(getClass(),
                    injector, new URL(url.toExternalForm().replace(oldName, newName)), newName);
        }
        catch (MalformedURLException e) {
            throw new AssertionError("Not a valid url: " + newName, e);
        }
    }

    /**
     * "Casts" this object into a subtype by creating the specified type.
     */
    public <T extends TopLevelItem> T as(Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        return newInstance(type, injector, url, name);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), url);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;

        if (!(other instanceof TopLevelItem)) return false;
        
        // Equals and hashCode must be consistent with each other.
        // We use URL only as the concrete class may not be detected, e.g. when obtaining a job
        // from a BuildHistory
        // TODO: equality checking in the presence of views.
        TopLevelItem rhs = (TopLevelItem) other;
        return url.toExternalForm().equals(rhs.url.toExternalForm());
    }

    @Override
    public int hashCode() {
        return url.toExternalForm().hashCode();
    }
}
