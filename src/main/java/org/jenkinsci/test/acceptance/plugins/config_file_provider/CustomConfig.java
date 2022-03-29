package org.jenkinsci.test.acceptance.plugins.config_file_provider;

import hudson.util.VersionNumber;
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
     * From config-file-provider:3.8.0, the content box of the custom files is a CodeMirror object instead of a textarea 
     * @param customContent the content to set
     */
    @Override
    public void content(String customContent) {
        final String PATH = "/config/content";
        boolean contentIsOldTextArea = this.getJenkins().getPlugin("config-file-provider").getVersion().isOlderThan(new VersionNumber("3.8.0"));

        if (contentIsOldTextArea) {
            control(PATH).set(customContent);
        } else {
            new CodeMirror(this, PATH).set(customContent);
        }
    }
}
