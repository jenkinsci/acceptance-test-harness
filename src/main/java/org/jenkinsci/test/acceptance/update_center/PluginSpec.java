/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.update_center;

import com.google.common.base.Splitter;
import hudson.util.VersionNumber;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

/**
 * Reference to a plugin, optionally with the version.
 *
 * The string syntax of this is shortName[@version].
 */
public class PluginSpec {

    /**
     * Short name of the plugin.
     */
    private final @Nonnull String name;

    /**
     * Optional version.
     */
    private final @CheckForNull VersionNumber version;

    public PluginSpec(@Nonnull String name, String version) {
        this.name = name;
        this.version = version == null
                ? null
                : new VersionNumber(version)
        ;
    }

    public PluginSpec(@Nonnull String coordinates) {
        Iterator<String> spliter = Splitter.on("@").split(coordinates).iterator();
        name = spliter.next();
        if (spliter.hasNext()) {
            version = new VersionNumber(spliter.next());
        } else {
            version = null;
        }
    }

    public @Nonnull String getName() {
        return name;
    }

    public @CheckForNull String getVersion() {
        return version == null ? null : version.toString();
    }

    public @CheckForNull VersionNumber getVersionNumber() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (version != null) {
            sb.append('@').append(version);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginSpec that = (PluginSpec) o;

        if (!name.equals(that.name)) return false;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }
}
