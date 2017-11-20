<?php
/**
 * SAML 2.0 IdP configuration for simpleSAMLphp.
 *
 * See: https://simplesamlphp.org/docs/stable/simplesamlphp-reference-idp-hosted
 */

$metadata['Idp-Service'] = array(
    /*
     * The hostname of the server (VHOST) that will use this SAML entity.
     *
     * Can be '__DEFAULT__', to use this entry by default.
     */
    'host' => '__DEFAULT__',

    /* X.509 key and certificate. Relative to the cert directory. */
    'privatekey' => 'server.pem',
    'certificate' => 'server.crt',

    /*
     * Authentication source to use. Must be one that is configured in
     * 'config/authsources.php'.
     */
    'auth' => 'example-userpass',

    /* [kuisathaverat] Using the uri NameFormat on attributes */
    'attributes.NameFormat' => 'urn:oasis:names:tc:SAML:2.0:attrname-format:uri',
    'authproc' => array(
    	// Convert LDAP names to oids.
    	100 => array('class' => 'core:AttributeMap', 'name2oid'),
    ),

    'SingleSignOnServiceBinding' => array(
        'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
        'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect',
    ),

    'SingleLogoutServiceBinding' => array(
        'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST',
        'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect',
    ),

    'signature.algorithm' => 'http://www.w3.org/2001/04/xmldsig-more#rsa-sha256',
    //'signature.algorithm' => 'http://www.w3.org/2001/04/xmldsig-more#rsa-sha384',
    //'signature.algorithm' => 'http://www.w3.org/2001/04/xmldsig-more#rsa-sha512',

    'redirect.sign' => FALSE,
    'redirect.validate' => FALSE,

    'saml20.sign.response' => TRUE,
    'saml20.sign.assertion' => TRUE,
    'NameIDFormat' => 'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
    'nameid.encryption' => FALSE,
    'assertion.encryption' => FALSE,
);
