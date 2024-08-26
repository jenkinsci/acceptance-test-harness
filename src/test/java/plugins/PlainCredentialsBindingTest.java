/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.AbstractCredentialsTest;
import org.jenkinsci.test.acceptance.plugins.credentials.BaseStandardCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.Domain;
import org.jenkinsci.test.acceptance.plugins.credentials.DomainPage;
import org.jenkinsci.test.acceptance.plugins.credentials.FileCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.StringCredentials;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.CredentialsBinding;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.ManagedCredentialsBinding;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.SecretFileCredentialsBinding;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.SecretStringCredentialsBinding;
import org.jenkinsci.test.acceptance.po.BatchCommandBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;

/**
 * Tests the plain-credentials and credentials-binding plugins together.
 */
@WithPlugins({"plain-credentials", "credentials-binding", "credentials"})
public class PlainCredentialsBindingTest extends AbstractCredentialsTest {

    private static final String BINDING_VARIABLE = "BINDING_VARIABLE";
    private static final String BINDING_SUCCESS = "BINDING_SUCCESS";

    @Test
    public void globalSecretFileCredentialTest() throws URISyntaxException {
        createAndUseCredential(GLOBAL_SCOPE, FileCredentials.class);
    }
    
    @Test
    public void globalSecretTextCredentialTest() throws URISyntaxException {
        createAndUseCredential(GLOBAL_SCOPE, StringCredentials.class);
    }
    
    @Test
    public void systemSecretFileCredentialTest() throws URISyntaxException {
        createAndUseCredential(SYSTEM_SCOPE, FileCredentials.class);
    }
    
    @Test
    public void systemSecretTextCredentialTest() throws URISyntaxException {
        createAndUseCredential(SYSTEM_SCOPE, StringCredentials.class);
    }
    
    @Test
    public void domainSecretFileCredentialTest() throws URISyntaxException {
        createAndUseDomainCredential(GLOBAL_SCOPE, FileCredentials.class);
    }

    @Test
    public void domainSecretTextCredentialsTest() throws URISyntaxException {
        createAndUseDomainCredential(GLOBAL_SCOPE, StringCredentials.class);
    }
    
    /**
     * Creates a credential of the type passed as parameter. 
     * Use it in a Free style job and performs relevant assertions.
     * 
     * @param scope
     * @param credentialClazz
     * @throws URISyntaxException
     */
    private <T extends BaseStandardCredentials> void createAndUseCredential(String scope, Class<T> credentialClazz) throws URISyntaxException {
        // Get credential binding class
        Class<? extends CredentialsBinding> credentialsBindingClazz = credentialClazz.isAssignableFrom(FileCredentials.class) ? SecretFileCredentialsBinding.class : SecretStringCredentialsBinding.class;
        
        // Create credential of whatever type has been passed as parameter
        final CredentialsPage cp = createCredentialsPage(false);
        createCredentials(credentialClazz, cp, null);

        // Create and configure job and perform relevant assertions
        createJobAndCheck(scope, credentialClazz, credentialsBindingClazz);
    }
    
    /**
     * Creates a credential of the type passed as parameter inside a domain. 
     * Use it in a Free style job and performs relevant assertions.
     * 
     * @param scope
     * @param credentialClazz
     * @throws URISyntaxException
     */
    private <T extends BaseStandardCredentials, K extends CredentialsBinding> void createAndUseDomainCredential(String scope, Class<T> credentialClazz) throws URISyntaxException {
        // Get credential binding class
        Class<? extends CredentialsBinding> credentialsBindingClazz = credentialClazz.isAssignableFrom(FileCredentials.class) ? SecretFileCredentialsBinding.class : SecretStringCredentialsBinding.class;
        
        final String domainName = "domain";
        
        // Create domain
        DomainPage dp = new DomainPage(jenkins);
        dp.open();
        Domain d = dp.addDomain();
        d.name.set(domainName);
        d.description.set("domain description");
        dp.save();
        
        // Create credential inside the domain
        final CredentialsPage c = createCredentialsPage(false, domainName);
        BaseStandardCredentials cred = createCredentials(credentialClazz, c, null);
        
        // Create and configure job and perform relevant assertions
        createJobAndCheck(scope, credentialClazz, credentialsBindingClazz);
    }

    /**
     * Creates a job and uses a credential in a shell script using the credentials binding functionality.
     * Perform relevant assertions.
     * 
     * @param scope
     * @param credentialClazz
     * @param credentialsBindingClazz
     */
    private <K extends CredentialsBinding, T extends BaseStandardCredentials> void createJobAndCheck(String scope,
            Class<T> credentialClazz, Class<K> credentialsBindingClazz) {
        // Create free style job
        FreeStyleJob job = jenkins.jobs.create();
        
        job.configure();
        job.check("Use secret text(s) or file(s)");
        
        // Add a credential binding
        final ManagedCredentialsBinding mcb = new ManagedCredentialsBinding(job);
        K cb = mcb.addCredentialBinding(credentialsBindingClazz);
        
        // If the scope is GLOBAL, job has access to credentials
        if (scope.equals(GLOBAL_SCOPE)) {
            // No need to select credential. Since there is only one it is already selected.
            cb.variable.set(BINDING_VARIABLE);
            
            // Add build step that echoes the value of the variable
            addBuildStepEchoingCredentials(job, credentialClazz);

            job.save();
            Build b = job.scheduleBuild();
    
            // Build should succeed
            b.shouldSucceed();
            // Binding should be OK
            assertThat(b.getConsole(), containsString(BINDING_SUCCESS));
        } else {
            // If the scope is SYSTEM, there should not be any credentials available
            assertTrue(cb.noCredentials());
        }
    }


    private <T extends BaseStandardCredentials> void addBuildStepEchoingCredentials(FreeStyleJob job, Class<T> credentialClazz) {
        if (SystemUtils.IS_OS_UNIX) {
            ShellBuildStep shell = job.addBuildStep(ShellBuildStep.class);

            if (credentialClazz.isAssignableFrom(FileCredentials.class)) {
                shell.command("if echo \"$BINDING_VARIABLE\" | grep -q \"" + SECRET_FILE + "\"  \n then \n echo \"" + BINDING_SUCCESS + "\" \n fi");
            } else {
                shell.command("if [ \"$" + BINDING_VARIABLE + "\" = \"" + SECRET_TEXT + "\" ] \n then \n echo \"" + BINDING_SUCCESS + "\" \n fi");
            }
        } else {
            BatchCommandBuildStep shell = job.addBuildStep(BatchCommandBuildStep.class);

            if (credentialClazz.isAssignableFrom(FileCredentials.class)) {
                shell.command("@echo \"%BINDING_VARIABLE%\" | findstr \"" + SECRET_FILE + "\">NUL && echo \"" + BINDING_SUCCESS + "\"");
            } else {
                shell.command("@if \"%" + BINDING_VARIABLE + "%\"==\"" + SECRET_TEXT + "\" echo \"" + BINDING_SUCCESS + "\"");
            }
        }
    }

}
