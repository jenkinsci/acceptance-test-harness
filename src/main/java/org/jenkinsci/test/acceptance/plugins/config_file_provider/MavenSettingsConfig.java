package org.jenkinsci.test.acceptance.plugins.config_file_provider;

import org.jenkinsci.test.acceptance.po.CodeMirror;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Class for Maven Settings files.
 */
@Describable("Maven settings.xml")
public class MavenSettingsConfig extends ProvidedFile {

    public final Control replaceAll = control("/config/isReplaceAll");

    public MavenSettingsConfig(ConfigFileProvider context, String id) {
        super(context, id);
    }

    @Override
    public void content(String mvnSettings) {
        new CodeMirror(this, "/config/content").set(mvnSettings);
    }

    public void replaceAll(final boolean replaceAll) {
        this.replaceAll.check(replaceAll);
    }

    public ServerCredentialMapping addServerCredentialMapping() {
        final String path = createPageArea("/config/serverCredentialMappings", new Runnable() {
            @Override public void run() {
                control("/config/repeatable-add").click();
            }
        });

        return new ServerCredentialMapping(this, path);
    }

}
