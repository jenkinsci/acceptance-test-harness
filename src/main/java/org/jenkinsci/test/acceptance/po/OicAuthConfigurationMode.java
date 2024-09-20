package org.jenkinsci.test.acceptance.po;

/**
 * Class representing the entry controls for the configuration mode when using the oic-auth plugin
 */
public abstract class OicAuthConfigurationMode extends PageAreaImpl {

    protected OicAuthConfigurationMode(OicAuthSecurityRealm realm) {
        super(realm, "serverConfiguration");
    }

    /**
     * Class representing the entry controls for well-known endpoint when using the oic-auth plugin
     */
    @Describable("Discovery via well-known endpoint")
    public static class WellKnownEndpoint extends OicAuthConfigurationMode {

        public final Control wellKnownEndpoint = control("wellKnownOpenIDConfigurationUrl");

        public WellKnownEndpoint(OicAuthSecurityRealm realm) {
            super(realm);
        }
    }

}
