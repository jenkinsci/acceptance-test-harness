package org.jenkinsci.test.acceptance;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.inject.Named;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsControllerFactory;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.server.JenkinsControllerPoolProcess;
import org.jenkinsci.test.acceptance.server.PooledJenkinsController;
import org.jenkinsci.test.acceptance.slave.LocalSlaveProvider;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;
import org.jenkinsci.test.acceptance.utils.mail.MailService;
import org.jenkinsci.test.acceptance.utils.mail.Mailtrap;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ConsoleExercisedPluginReporter;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ExercisedPluginsReporter;
import org.jenkinsci.test.acceptance.utils.pluginreporter.TextFileExercisedPluginReporter;
import org.openqa.selenium.WebDriver;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * The default configuration for running tests.
 * <p>
 * See {@link Config} for how to override it.
 *
 * @author Kohsuke Kawaguchi
 */
public class FallbackConfig extends AbstractModule {

    @Override
    protected void configure() {
        // default in case nothing is specified
        bind(SlaveProvider.class).to(LocalSlaveProvider.class);

        // default email service provider
        bind(MailService.class).to(Mailtrap.class);

        // WebDriver provider
        bind(WebDriver.class).toProvider(WebDriverProvider.class).in(TestScope.class);
    }

    @Provides
    public ElasticTime getElasticTime() {
        return new ElasticTime();
    }

    /**
     * Instantiates a controller through the "TYPE" attribute and {@link JenkinsControllerFactory}.
     */
    @Provides @TestScope
    public JenkinsController createController(Injector injector, ExtensionList<JenkinsControllerFactory> factories) throws IOException {
        String type = System.getenv("type");  // this is lower case for backward compatibility
        if (type==null)
            type = System.getenv("TYPE");
        if (type==null) {
            if (JenkinsControllerPoolProcess.SOCKET.exists() && !JenkinsControllerPoolProcess.MAIN)
                return new PooledJenkinsController(injector);
            else
                type = "winstone";
        }

        for (JenkinsControllerFactory f : factories) {
            if (f.getId().equalsIgnoreCase(type)) {
                final JenkinsController c = f.create();
                c.postConstruct(injector);
                return c;
            }
        }

        throw new AssertionError("Invalid controller type: "+type);
    }

    @Provides @TestScope
    public Jenkins createJenkins(Injector injector, JenkinsController controller) {
        if (!controller.isRunning()) return null;
        return new Jenkins(injector, controller);
    }

    /**
     * Provides the path to the form elements plug-in. Uses the Maven repository to obtain the plugin.
     *
     * @return the path to the form elements plug-in
     */
    @Named("form-element-path.hpi") @Provides
    public File getFormElementsPathFile(RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession) {
        ArtifactResolverUtil resolverUtil = new ArtifactResolverUtil(repositorySystem, repositorySystemSession);
        ArtifactResult resolvedArtifact = resolverUtil.resolve(new DefaultArtifact("org.jenkins-ci.plugins", "form-element-path", "hpi", "1.4"));
        return resolvedArtifact.getArtifact().getFile();
    }

    /**
     * directory on the computer where this code is running that points to a directory
     * where test code can place log files, cache files, etc.
     * Note that this directory might not exist on the Jenkins master, since it can be
     * running on a separate computer.
     */
    @Provides @Named("WORKSPACE")
    public String getWorkspace() {
        String ws = System.getenv("WORKSPACE");
        if (ws != null) return ws;
        return new File(System.getProperty("user.dir"), "target").getPath();
    }

    /**
     * Get the file with Jenkins to run.
     *
     * The file will exist on machine where tests run.
     */
    @Provides @Named("jenkins.war")
    public File getJenkinsWar(RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession) {
        try {
            return IOUtil.firstExisting(false, System.getenv("JENKINS_WAR"));
        } catch (IOException ex) {
            // Fall-through
        }

        String version = System.getenv("JENKINS_VERSION");
        if (version != null && !version.isEmpty()) {
            ArtifactResolverUtil resolverUtil = new ArtifactResolverUtil(repositorySystem, repositorySystemSession);
            ArtifactResult resolvedArtifact = resolverUtil.resolve(new DefaultArtifact("org.jenkins-ci.main", "jenkins-war", "war", version));
            return resolvedArtifact.getArtifact().getFile();
        }

        // TODO add support for 'lts', 'lts-rc', 'latest' and 'latest-rc'

        try {
            // Lowest priority of all
            return IOUtil.firstExisting(false, getWorkspace() + "/jenkins.war", "./jenkins.war");
        } catch (IOException ex) {
            // Fall-through
        }

        throw new Error("Could not find jenkins.war, use JENKINS_WAR or JENKINS_VERSION to specify it.");
    }

    /**
     * Switch to control if an existing plugin should be updated.
     *
     * <p>
     * If true, a test will be skipped when it requires a newer version of a plugin that's already installed.
     * If false, an existing plugin will be updated to the requirements of the test.
     */
    @Provides @Named("neverReplaceExistingPlugins")
    public boolean neverReplaceExistingPlugins() {
        return System.getenv("NEVER_REPLACE_EXISTING_PLUGINS") != null;
    }

    /**
     *  Provides a mechanism to create a report on which plugins were used
     *  during the test execution
     *
     *  @return An ExercisedPluginReporter based on env var EXERCISEDPLUGINREPORTER
     */
     @Provides @Named("ExercisedPluginReporter")
     public ExercisedPluginsReporter createExercisedPluginReporter() {
         String reporter = System.getenv("EXERCISEDPLUGINREPORTER");
         if (reporter==null) {
             reporter = "console";
         } else {
             reporter = reporter.toLowerCase(Locale.ENGLISH);
         }

         switch (reporter) {
         case "console":
             return new ConsoleExercisedPluginReporter();
         case "textfile":
             return TextFileExercisedPluginReporter.getInstance();
         default:
             throw new AssertionError("Unrecognized Exercised Plugin Report type: "+reporter);
         }
     }
}

