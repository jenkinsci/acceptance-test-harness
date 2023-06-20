/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
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

package org.jenkinsci.test.acceptance.utils.aether;

import jakarta.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

/**
 * Helper class to resolve artifacts with Aether
 * with http proxy support
 *
 * @author scott.hebert@ericsson.com
 */
public class ArtifactResolverUtil {

    private static final Logger LOGGER = Logger.getLogger(ArtifactResolverUtil.class.getName());
    private RepositorySystem repoSystem;
    private RepositorySystemSession repoSystemSession;

    @Inject
    public ArtifactResolverUtil(RepositorySystem rs, RepositorySystemSession rss) {
        repoSystem = rs;
        repoSystemSession = rss;
    }

    /**
     * @param artifact The artifact to be resolved
     *
     * @return artifact resolution result
     */
    public ArtifactResult resolve(DefaultArtifact artifact) {
        Builder repoBuilder = new RemoteRepository.Builder(
                "repo.jenkins-ci.org", "default",
                "https://repo.jenkins-ci.org/public/");

        DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();

        File userHome = new File(System.getProperty("user.home"));
        File userSettingsFile = new File(new File(userHome, ".m2"), "settings.xml");
        request.setUserSettingsFile(userSettingsFile);

        if (userSettingsFile.exists()) {
            LOGGER.config("Found maven settings file - " + userSettingsFile.getAbsolutePath());
            SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

            try {
                Settings settings = settingsBuilder.build(request).getEffectiveSettings();
                org.apache.maven.settings.Proxy mavenActiveproxy = settings.getActiveProxy();
                if (mavenActiveproxy != null) {
                    LOGGER.config("Found maven proxy settings. Will use for artifact resolution");
                    repoBuilder.setProxy(asProxy(mavenActiveproxy));
                } else {
                    LOGGER.config("Did not find an active proxy in maven settings xml file");
                }
            } catch (SettingsBuildingException e) {
                LOGGER.log(Level.WARNING, "Could not find or load settings.xml to attempt to user proxy settings.", e);
            }
        }

        RemoteRepository repo = repoBuilder.build();
        ArtifactResult r;
        try {
            r = repoSystem.resolveArtifact(repoSystemSession,new ArtifactRequest(artifact, Arrays.asList(repo), null));
        } catch (ArtifactResolutionException e) {
            throw new RuntimeException("Could not resolve " + artifact + " from Maven repository",e);
        }
        LOGGER.config("Found " + r);
        return r;
    }

    /**
     * Converts Maven Proxy to Aether Proxy
     *
     * @param proxy the Maven proxy to be converted
     * @return Aether proxy equivalent
     */
    private static Proxy asProxy(org.apache.maven.settings.Proxy proxy) {
        Authentication auth = null;
        if (proxy.getUsername() != null && proxy.getPassword() != null) {
            AuthenticationBuilder authBuilder = new AuthenticationBuilder();
            authBuilder.addUsername(proxy.getUsername());
            authBuilder.addPassword(proxy.getPassword());
            auth = authBuilder.build();
        }
        return new Proxy(proxy.getProtocol(), proxy.getHost(), proxy.getPort(), auth);
    }
}
