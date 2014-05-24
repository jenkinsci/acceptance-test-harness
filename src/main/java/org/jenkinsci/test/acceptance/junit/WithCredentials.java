package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a test requires credentials.
 * Adds required credentials in the credential plugin
 * <p/>
 * Supports:
 * <p/>
 * -Username/password:
 * <p/>
 * Example: @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {"username", "password"})
 * <p/>
 * -Username/sshKey:
 * <p/>
 * Example: @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"username", "/ssh_keys/unsafe"})
 * <p/>
 *
 * @author Karl Matthias
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(WithCredentials.RuleImpl.class)
public @interface WithCredentials {

    public static final int USERNAME_PASSWORD = 1;

    public static final int SSH_USERNAME_PRIVATE_KEY = 2;

    int credentialType();

    String[] values();

    public class RuleImpl implements TestRule {

        @Inject
        Jenkins jenkins;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    enterCredentials(d.getAnnotation(WithCredentials.class));
                    enterCredentials(d.getTestClass().getAnnotation(WithCredentials.class));

                    base.evaluate();
                }

                private boolean enterCredentials(WithCredentials wp) {
                    if (wp != null) {
                        switch (wp.credentialType()) {
                            case USERNAME_PASSWORD:
                                if (wp.values().length == 2) {
                                    addUsernamePasswordCredentials(wp.values()[0], wp.values()[1]);
                                } else {
                                    throw new RuntimeException("@WithCredentials: Wrong amount of values. Expected username,password. ");
                                }
                                break;
                            case SSH_USERNAME_PRIVATE_KEY:
                                if (wp.values().length == 2) {
                                    addSshUsernamePrivateKeyCredentials(wp.values()[0], wp.values()[1]);
                                } else {
                                    throw new RuntimeException("@WithCredentials: Wrong amount of values. Expected username,sshKeyPath.");
                                }
                                break;
                            default:
                                throw new RuntimeException(String.format("@WithCredentials: Option '%s' not supported.", wp.credentialType()));
                        }
                    }
                    return wp != null;
                }

                /**
                 * Adds a username and sshKey in the credentials plugin
                 * @param username ssh username
                 * @param sshKeyPath path to the ssh key
                 */
                private void addSshUsernamePrivateKeyCredentials(String username, String sshKeyPath) {
                    ManagedCredentials c = new ManagedCredentials(jenkins);
                    c.open();
                    SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
                    sc.username.set(username);
                    sc.selectEnterDirectly().privateKey.set(resource(sshKeyPath).asText());
                    c.save();
                }

                /**
                 * Adds a username and password in the credentials plugin
                 * @param username username
                 * @param password password
                 */
                private void addUsernamePasswordCredentials(String username, String password) {
                    final ManagedCredentials c = new ManagedCredentials(jenkins);
                    c.open();
                    final UserPwdCredential upc = c.add(UserPwdCredential.class);
                    upc.username.set(username);
                    upc.password.set(password);
                    c.save();
                }


                /**
                 * Obtains a resource in a wrapper.
                 */
                public Resource resource(String path) {
                    final URL resource = getClass().getResource(path);
                    if (resource == null)
                        throw new AssertionError("No such resource " + path + " for " + getClass().getName());
                    return new Resource(resource);
                }
            };


        }
    }
}
