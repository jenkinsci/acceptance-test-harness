# Kerberos KDC for testing

This is an alternative to automated test suitable for manual testing/investigation.

## Start KDC

Run:

    $ git clone git@github.com:jenkinsci/acceptance-test-harness.git
    $ cd src/main/resources/org/jenkinsci/test/acceptance/docker/fixtures/KerberosContainer/
    $ ./run-kdc.sh

## Start Jenkins

Run:

    $ cd kerberos-sso-plugin
    $ mvn clean hpi:run -Dorg.kohsuke.stapler.compression.CompressionFilter.disabled=true -Dsun.security.krb5.debug=true -Dsun.security.spnego.debug=true

Configure backend security realm and kerberos sso with file names provided in Start KDC step. Note it is enough to switch
`<enabled>true</enabled>` to `<enabled>false</enabled>` in `work/kerberos-sso.xml` and restart to turn it off again.

## Run the test

Run:

    $ cd src/main/resources/org/jenkinsci/test/acceptance/docker/fixtures/KerberosContainer/
    $ env KRB5_CONFIG=./target/etc.krb5.conf KRB5CCNAME=./target/client.keytab kinit -k -t ./target/keytab/user user
    $ env KRB5_CONFIG=./target/etc.krb5.conf KRB5CCNAME=./target/client.keytab KRB5_TRACE=target/tracelog curl -vL --negotiate -u : http://localhost:8080/jenkins/whoAmI
