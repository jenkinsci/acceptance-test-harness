package org.jenkinsci.test.acceptance.plugins.config_file_provider;

import org.jenkinsci.test.acceptance.po.CodeMirror;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Class for custom config files.
 */
@Describable("Custom file")
public class CustomConfig extends ProvidedFile {

    public CustomConfig(ConfigFileProvider context, String id) {
        super(context, id);
    }

    /**
     * @param customContent the content to set
     */
    @Override
    public void content(String customContent) {
        new CodeMirror(this, "/config/content").set(customContent);
    }
}
