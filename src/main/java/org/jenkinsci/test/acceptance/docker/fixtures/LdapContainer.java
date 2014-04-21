package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * openLDAP (slapd) Container with a small user directory.
 *
 * @author Michael Prankl
 */
@DockerFixture(id = "ldap", ports = {389, 636})
public class LdapContainer extends DockerContainer {

    public String getHost() {
        return "127.0.0.1";
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

}
