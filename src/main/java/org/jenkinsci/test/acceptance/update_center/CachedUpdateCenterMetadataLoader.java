package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.po.Jenkins;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Parses update-center.json.html possibly from a cache and determine plugins to install.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class CachedUpdateCenterMetadataLoader implements UpdateCenterMetadataProvider {
    UpdateCenterMetadata metadata;

    @Inject(optional=true) @Named("update_center_url_cache")
    File cacheBase = new File(System.getProperty("java.io.tmpdir"), "update-center");

    @Inject(optional=true) @Named("update_center_url")
    String url = "https://updates.jenkins.io/update-center.json"; // TODO consider using update-center.actual.json so we do not need to strip preamble/postamble

    @Inject
    ExtensionList<UpdateCenterMetadataDecorator> decorators;

    @Override
    public UpdateCenterMetadata get(Jenkins jenkins) throws IOException {
        if (metadata==null) {
            String version = jenkins.getVersion().toString();
            File cache = new File(cacheBase + "-" + version + ".jsonp");
            if (!cache.exists() || System.currentTimeMillis()-cache.lastModified() > TimeUnit.DAYS.toMillis(1)) {
                // load cache
                URL versionedUrl = new URL(url + "?version=" + version);
                System.err.println("Downloading " + versionedUrl + " to " + cache);
                FileUtils.copyURLToFile(versionedUrl, cache);
            } else {
                System.err.println("Using cached " + cache);
            }
            metadata = UpdateCenterMetadata.parse(cache);
            for (UpdateCenterMetadataDecorator decorator : decorators) {
                decorator.decorate(metadata);
            }
        }
        return metadata;
    }
}
