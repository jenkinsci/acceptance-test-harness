package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.util.Arrays;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import com.google.inject.Inject;

/**
 * Base class for Jenkins controller factories. Provides access to the form elements plug-in.
 *
 * @author Ullrich Hafner
 */
public abstract class AbstractJenkinsControllerFactory implements JenkinsControllerFactory {
    @Inject
    private RepositorySystem repositorySystem;
    @Inject
    private RepositorySystemSession repositorySystemSession;

    /**
     * Returns the path to the form elements plug-in. Uses the Maven repository to obtain the plugin.
     *
     * @return the path to the form elements plug-in
     */
    protected File getFormElementsPathFile() {
        try {
            ArtifactResult resolvedArtifact = repositorySystem.resolveArtifact(repositorySystemSession,
                    new ArtifactRequest(new DefaultArtifact("org.jenkins-ci.plugins", "form-element-path", "hpi", "1.4"),
                            Arrays.asList(new RemoteRepository.Builder("repo.jenkins-ci.org", "default", "http://repo.jenkins-ci.org/public/").build()),
                            null));
            return resolvedArtifact.getArtifact().getFile();
        }
        catch (ArtifactResolutionException e) {
            throw new RuntimeException("Could not resolve form-element-path.hpi from Maven repository repo.jenkins-ci.org.", e);
        }
    }
}
