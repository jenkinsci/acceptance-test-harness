package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.plugins.ldap.LdapDetails;

/**
 * openLDAP (slapd) Container with a small user directory.
 * The configuration of this ldap is located next to its Dockerfile.
 * To edit/add users or groups just add them to config/base.ldif and rebuild the image.
 *
 * @author Michael Prankl
 */
@DockerFixture(id = "ldap", ports = {389, 636})
public class LdapContainer extends DockerContainer {

    public String getHost() {
        return ipBound(389);
    }

    public int getPort() {
        return port(389);
    }

    public String getRootDn() {
        return "dc=jenkins-ci,dc=org";
    }

    public String getManagerDn() {
        return "cn=admin," + getRootDn();
    }

    public String getManagerPassword() {
        return "jenkins";
    }

    /**
     * @return default ldap connection details from current running docker LdapContainer.
     */
    public LdapDetails createDefault() {
        String host = getHost();
        if (ipv6Enabled()) {
            host = encloseInBrackets(host);
        }
        return new LdapDetails(host, getPort(), getManagerDn(), getManagerPassword(), getRootDn());
    }

}
