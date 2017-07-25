package org.jenkinsci.test.acceptance.plugins.foreman_node_sharing;

import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.po.Cloud;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.FormValidation;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Foreman Cloud.
 *
 */
@Describable("Foreman Shared Node")
public class ForemanSharedNodeCloudPageArea extends Cloud {

    /**
     * Control for credentials.
     */
    private final Control credentialsId = control("credentialsId");
    private String name;

    /**
     * Constructor.
     * @param context page area.
     * @param path path of pa.
     */
    public ForemanSharedNodeCloudPageArea(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Set name.
     * @param value name.
     * @return ForemanCloudPageArea.
     */
    public ForemanSharedNodeCloudPageArea name(String value) {
        control("cloudName").set(value);
        this.name = value;
        return this;
    }

    /**
     * Set user.
     * @param value user.
     * @return ForemanCloudPageArea.
     */
    public ForemanSharedNodeCloudPageArea user(String value) {
        control("user").set(value);
        return this;
    }

    /**
     * Set url.
     * @param value url.
     * @return ForemanCloudPageArea.
     */
    public ForemanSharedNodeCloudPageArea url(String value) {
        control("url").set(value);
        return this;
    }

    /**
     * Set password.
     * @param value password.
     * @return ForemanCloudPageArea.
     */
    public ForemanSharedNodeCloudPageArea password(String value) {
        control("password").set(value);
        return this;
    }

    /**
     * Test connection.
     * @return ForemanCloudPageArea.
     */
    public FormValidation testConnection() {
        Control button = control("validate-button");
        button.click();
        return button.getFormValidation();
    }

    /**
     * Add credential.
     * @return dialog.
     */
    public SshCredentialDialog addCredential() {
        self().findElement(by.button("Add")).click();

        return new SshCredentialDialog(getPage(), "/credentials");
    }

    /**
     * Set credentials.
     * @param string credentials.
     * @return ForemanCloudPageArea.
     */
    public ForemanSharedNodeCloudPageArea setCredentials(String string) {
        credentialsId.select(string);
        return this;
    }

    /**
     * Checks for compatible hosts.
     * @return ForemanCloudPageArea.
     */
    public ForemanSharedNodeCloudPageArea checkForCompatibleHosts() {
        clickButton("Check for Compatible Foreman Hosts");
        return this;
    }

    /**
     * Get Cloud name.
     * @return name.
     */
    public String getCloudName() {
        return name;
    }


}
