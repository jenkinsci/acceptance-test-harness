package org.jenkinsci.test.acceptance.update_center;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.commons.io.FileUtils;

import javax.inject.Named;
import javax.inject.Singleton;
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
public class CachedUpdateCenterMetadataLoader implements Provider<UpdateCenterMetadata>, javax.inject.Provider<UpdateCenterMetadata> {
    UpdateCenterMetadata metadata;

    @Inject(optional=true) @Named("update_center_url_cache")
    File cache = new File(System.getProperty("java.io.tmpdir"), "update-center.json");

    @Inject(optional=true) @Named("update_center_url")
    String url = "https://updates.jenkins-ci.org/update-center.json";

    @Inject
    ExtensionList<UpdateCenterMetadataDecorator> decorators;

    @Override
    public UpdateCenterMetadata get() {
        try {
            if (metadata==null) {
                if (!cache.exists() || System.currentTimeMillis()-cache.lastModified() > TimeUnit.DAYS.toMillis(1)) {
                    // load cache
                    FileUtils.copyURLToFile(new URL(url),cache);
                }
                metadata = UpdateCenterMetadata.parse(cache);
                for (UpdateCenterMetadataDecorator decorator : decorators) {
                    decorator.decorate(metadata);
                }
            }
            return metadata;
        } catch (IOException e) {
            throw new AssertionError("Failed to parse update center data of "+url+" at "+cache, e);
        }
    }
}
