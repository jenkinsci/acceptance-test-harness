package org.jenkinsci.test.acceptance.plugins.config_file_provider;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Class for Server Credentials in Maven Settings files.
 */
public class ServerCredentialMapping extends PageAreaImpl {

    public final Control serverId = control("serverId");
    public final Control credentialsId = control("credentialsId");

    public ServerCredentialMapping(final MavenSettingsConfig mvnSettingsConfig, String path) {
        super(mvnSettingsConfig, path);
    }

    public void serverId(final String id) {
        this.serverId.sendKeys(id);
    }

    public void credentialsId(final String credId) {
        this.credentialsId.select(credId);
    }
}
