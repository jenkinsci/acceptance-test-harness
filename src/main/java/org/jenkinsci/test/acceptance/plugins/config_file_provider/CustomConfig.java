package org.jenkinsci.test.acceptance.plugins.config_file_provider;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Class for custom config files.
 */
@Describable("org.jenkinsci.plugins.configfiles.custom.CustomConfig")
public class CustomConfig extends ProvidedFile {

    public final Control content = control("/config/content");

    public CustomConfig(ConfigFileProvider context, String id) {
        super(context, id);
    }

    @Override
    public void content(String customContent) {
        this.content.set(customContent);
    }

}
