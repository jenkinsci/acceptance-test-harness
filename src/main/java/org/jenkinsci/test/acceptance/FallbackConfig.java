package org.jenkinsci.test.acceptance;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsControllerFactory;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.selenium.SanityChecker;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.jenkinsci.test.acceptance.server.JenkinsControllerPoolProcess;
import org.jenkinsci.test.acceptance.server.PooledJenkinsController;
import org.jenkinsci.test.acceptance.slave.LocalSlaveProvider;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.jenkinsci.test.acceptance.utils.SauceLabsConnection;
import org.jenkinsci.test.acceptance.utils.SeleniumGridConnection;
import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;
import org.jenkinsci.test.acceptance.utils.mail.MailService;
import org.jenkinsci.test.acceptance.utils.mail.Mailtrap;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ConsoleExercisedPluginReporter;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ExercisedPluginsReporter;
import org.jenkinsci.test.acceptance.utils.pluginreporter.TextFileExercisedPluginReporter;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * The default configuration for running tests.
 * <p>
 * See {@link Config} for how to override it.
 *
 * @author Kohsuke Kawaguchi
 */
public class FallbackConfig extends AbstractModule {
    /** Browser property to set the default locale. */
    private static final String LANGUAGE_SELECTOR = "intl.accept_languages";

    /**
     * PhantomJS browser property to set the default locale.
     */
    private static final String LANGUAGE_SELECTOR_PHANTOMJS = "phantomjs.page.customHeaders.Accept-Language";

    @Override
    protected void configure() {
        // default in case nothing is specified
        bind(SlaveProvider.class).to(LocalSlaveProvider.class);

        // default email service provider
        bind(MailService.class).to(Mailtrap.class);
    }

    private WebDriver createWebDriver(TestName testName) throws IOException {
        String browser = System.getenv("BROWSER");
        if (browser==null) browser = "firefox";
        browser = browser.toLowerCase(Locale.ENGLISH);

        switch (browser) {
        case "firefox":
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference(LANGUAGE_SELECTOR, "en");

            return new FirefoxDriver(profile);
        case "ie":
        case "iexplore":
        case "iexplorer":
            return new InternetExplorerDriver();
        case "chrome":
            Map<String, String> prefs = new HashMap<String, String>();
            prefs.put(LANGUAGE_SELECTOR, "en");
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);

            return new ChromeDriver(options);
        case "safari":
            return new SafariDriver();
        case "htmlunit":
            return new HtmlUnitDriver(true);
        case "saucelabs":
        case "saucelabs-firefox":
            DesiredCapabilities caps = DesiredCapabilities.firefox();
            caps.setCapability("version", "29");
            caps.setCapability("platform", "Windows 7");
            caps.setCapability("name", testName.get());

            // if running inside Jenkins, expose build ID
            String tag = System.getenv("BUILD_TAG");
            if (tag!=null)
                caps.setCapability("build", tag);

            return new SauceLabsConnection().createWebDriver(caps);
        case "phantomjs":
            DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
            capabilities.setCapability(LANGUAGE_SELECTOR, "en");
            capabilities.setCapability(LANGUAGE_SELECTOR_PHANTOMJS, "en");
            return new PhantomJSDriver(capabilities);

        case "seleniumgrid":
                DesiredCapabilities cap = new DesiredCapabilities();

                //Get selenium grid properties
                Properties properties = new Properties();
                try {
                    properties.load(new FileInputStream("src/main/resources/seleniumGrid.properties"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cap.setBrowserName(properties.getProperty("browserval"));

                return new SeleniumGridConnection().createWebDriver(cap);

        default:
            throw new Error("Unrecognized browser type: "+browser);
        }
    }

    /**
     * Creates a {@link WebDriver} for each test, then make sure to clean it up at the end.
     */
    @Provides @TestScope
    public WebDriver createWebDriver(TestCleaner cleaner, TestName testName, ElasticTime time) throws IOException {
        WebDriver base = createWebDriver(testName);
        Dimension oldSize = base.manage().window().getSize();
        if (oldSize.height < 768 || oldSize.width < 1024) {
            base.manage().window().setSize(new Dimension(1024, 768));
        }

        final EventFiringWebDriver d = new EventFiringWebDriver(base);
        d.register(new SanityChecker());
        d.register(new Scroller());

        try {
            d.manage().timeouts().pageLoadTimeout(time.seconds(30), TimeUnit.MILLISECONDS);
            d.manage().timeouts().implicitlyWait(time.seconds(1), TimeUnit.MILLISECONDS);
        } catch (UnsupportedCommandException e) {
            // sauce labs RemoteWebDriver doesn't support this
            System.out.println(base + " doesn't support page load timeout");
        }
        cleaner.addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                d.quit();
            }
        });
        return d;
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
            return IOUtil.firstExisting(false, System.getenv("JENKINS_WAR"), getWorkspace() + "/jenkins.war", "./jenkins.war");
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

