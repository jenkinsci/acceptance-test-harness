package org.jenkinsci.test.acceptance;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.client.ClientUtil;
import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import jakarta.inject.Named;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsControllerFactory;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.recorder.HarRecorder;
import org.jenkinsci.test.acceptance.recorder.TestRecorderRule;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.jenkinsci.test.acceptance.server.JenkinsControllerPoolProcess;
import org.jenkinsci.test.acceptance.server.PooledJenkinsController;
import org.jenkinsci.test.acceptance.slave.LocalSlaveProvider;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.jenkinsci.test.acceptance.utils.SauceLabsConnection;
import org.jenkinsci.test.acceptance.utils.aether.ArtifactResolverUtil;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ConsoleExercisedPluginReporter;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ExercisedPluginsReporter;
import org.jenkinsci.test.acceptance.utils.pluginreporter.TextFileExercisedPluginReporter;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;

/**
 * The configuration for running tests.
 *
 * @author Kohsuke Kawaguchi
 */
public class Config extends AbstractModule {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    /** Browser property to set the default locale. */
    private static final String LANGUAGE_SELECTOR = "intl.accept_languages";

    public static final String DOM_MAX_SCRIPT_RUN_TIME = "dom.max_script_run_time";
    public static final String DOM_MAX_CHROME_SCRIPT_RUN_TIME = "dom.max_chrome_script_run_time";
    public static final String DEVTOOLS_JSONVIEW_ENABLED = "devtools.jsonview.enabled";
    public static final String PASSWORD_MANAGER_LEAK_DETECTION = "profile.password_manager_leak_detection";
    public static final int PAGE_LOAD_TIMEOUT = 30;
    public static final int IMPLICIT_WAIT_TIMEOUT = 1;

    @Override
    protected void configure() {
        // default in case nothing is specified
        bind(SlaveProvider.class).to(LocalSlaveProvider.class);
    }

    private WebDriver createWebDriver(TestCleaner cleaner, TestName testName) throws IOException {
        String browser = getBrowser();

        String display = getBrowserDisplay();
        switch (browser) {
            case "firefox":
                setDriverPropertyIfMissing("geckodriver", GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
                GeckoDriverService.Builder builder = new GeckoDriverService.Builder();
                if (display != null) {
                    builder.withEnvironment(Map.of("DISPLAY", display));
                }
                if (System.getenv("FIREFOX_TRACE") != null) {
                    builder.withLogLevel(FirefoxDriverLogLevel.fromString(System.getenv("FIREFOX_TRACE")));
                    builder.withLogOutput(System.err);
                }
                GeckoDriverService service = builder.build();
                return new FirefoxDriver(service, buildFirefoxOptions(testName));
            case "firefox-container":
                return createContainerWebDriver(
                        cleaner, "selenium/standalone-firefox:4.35.0", buildFirefoxOptions(testName));
            case "chrome-container":
                return createContainerWebDriver(
                        cleaner, "selenium/standalone-chrome:4.35.0", buildChromeOptions(testName));
            case "chrome":
                return new ChromeDriver(buildChromeOptions(testName));
            case "safari":
                return new SafariDriver(buildSafariOptions(testName));
            case "saucelabs":
            case "saucelabs-firefox":
                FirefoxOptions caps = new FirefoxOptions();
                caps.setCapability("version", "29");
                caps.setCapability("platform", "Windows 7");
                caps.setCapability("name", testName.get());
                if (HarRecorder.isCaptureHarEnabled()) {
                    caps.setCapability(CapabilityType.PROXY, createSeleniumProxy(testName.get()));
                }

                // if running inside Jenkins, expose build ID
                String tag = System.getenv("BUILD_TAG");
                if (tag != null) {
                    caps.setCapability("build", tag);
                }

                return new SauceLabsConnection().createWebDriver(caps);
            case "remote-webdriver-firefox":
                return buildRemoteWebDriver(buildFirefoxOptions(testName));
            case "remote-webdriver-chrome":
                return buildRemoteWebDriver(buildChromeOptions(testName));
            default:
                throw new Error("Unrecognized browser type: " + browser);
        }
    }

    private WebDriver buildRemoteWebDriver(Capabilities options) {
        String u = System.getenv("REMOTE_WEBDRIVER_URL");
        if (StringUtils.isBlank(u)) {
            throw new Error("remote-webdriver type browsers require REMOTE_WEBDRIVER_URL to be set");
        }
        // http://192.168.99.100:4444/wd/hub
        WebDriver driver =
                RemoteWebDriver.builder().address(u).addAlternative(options).build();
        ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        return driver;
    }

    private String getBrowser() {
        String browser = System.getenv("BROWSER");
        if (browser == null) {
            browser = "firefox";
        }
        browser = browser.toLowerCase(Locale.ENGLISH);
        return browser;
    }

    private FirefoxOptions buildFirefoxOptions(TestName testName) throws IOException {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setImplicitWaitTimeout(Duration.ofSeconds(IMPLICIT_WAIT_TIMEOUT));
        firefoxOptions.setPageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));

        firefoxOptions.addPreference(LANGUAGE_SELECTOR, "en");
        // Config screen with many plugins can cause FF to complain JS takes too long to complete - set longer timeout
        firefoxOptions.addPreference(
                DOM_MAX_SCRIPT_RUN_TIME, (int) getElasticTime().seconds(600));
        firefoxOptions.addPreference(
                DOM_MAX_CHROME_SCRIPT_RUN_TIME, (int) getElasticTime().seconds(600));
        firefoxOptions.addPreference(DEVTOOLS_JSONVIEW_ENABLED, false); // For MetricsTest
        firefoxOptions.enableBiDi();
        if (HarRecorder.isCaptureHarEnabled()) {
            firefoxOptions.setProxy(createSeleniumProxy(testName.get()));
        }
        if (System.getenv("FIREFOX_BIN") != null) {
            firefoxOptions.setBinary(System.getenv("FIREFOX_BIN"));
        }
        firefoxOptions.setCapability("se:name", testName.get());
        firefoxOptions.setCapability(
                "se:recordVideo", TestRecorderRule.isRecorderEnabled() && System.getenv("VIDEO_FOLDER") != null);
        return firefoxOptions;
    }

    private ChromeOptions buildChromeOptions(TestName testName) throws IOException {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setImplicitWaitTimeout(Duration.ofSeconds(IMPLICIT_WAIT_TIMEOUT));
        chromeOptions.setPageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));

        chromeOptions.setExperimentalOption(
                "prefs", Map.of(LANGUAGE_SELECTOR, "en", PASSWORD_MANAGER_LEAK_DETECTION, false));
        chromeOptions.enableBiDi();
        if (HarRecorder.isCaptureHarEnabled()) {
            chromeOptions.setProxy(createSeleniumProxy(testName.get()));
        }
        setDriverPropertyIfMissing("chromedriver", ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
        chromeOptions.setCapability("se:name", testName.get());
        chromeOptions.setCapability(
                "se:recordVideo", TestRecorderRule.isRecorderEnabled() && System.getenv("VIDEO_FOLDER") != null);
        return chromeOptions;
    }

    private SafariOptions buildSafariOptions(TestName testName) throws IOException {
        SafariOptions safariOptions = new SafariOptions();
        safariOptions.setImplicitWaitTimeout(Duration.ofSeconds(IMPLICIT_WAIT_TIMEOUT));
        safariOptions.setPageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));

        // no bidi support :(
        // safariOptions.enableBiDi();
        if (HarRecorder.isCaptureHarEnabled()) {
            safariOptions.setProxy(createSeleniumProxy(testName.get()));
        }
        safariOptions.setCapability("se:name", testName.get());
        safariOptions.setCapability(
                "se:recordVideo", TestRecorderRule.isRecorderEnabled() && System.getenv("VIDEO_FOLDER") != null);
        return safariOptions;
    }

    private WebDriver createContainerWebDriver(TestCleaner cleaner, String image, MutableCapabilities capabilities)
            throws IOException {
        try {
            final int controlPort = IOUtil.randomTcpPort();
            final int vncPort = IOUtil.randomTcpPort(5900, 6000);
            final int displayNumber = vncPort - 5900;

            Path log = Files.createTempFile("ath-docker-browser", "log");
            LOGGER.info("Starting selenium container '" + image + "'. Logs in " + log);

            Docker.cmd("pull", image).popen().verifyOrDieWith("Failed to pull image " + image);
            // While this only needs to expose two ports (controlPort, vncPort), it needs to be able to talk to Jenkins
            // running
            // out of container so using host networking is the most straightforward way to go.
            String[] args = {
                "run",
                "-d",
                "--shm-size=2g",
                "--network=host",
                "-e",
                "SE_OPTS=--port " + controlPort,
                "-e",
                "DISPLAY=:" + displayNumber + ".0",
                "-e",
                "DISPLAY_NUM=" + displayNumber,
                "-e",
                "SE_VNC_PORT=" + vncPort,
                image
            };
            ProcessInputStream popen = Docker.cmd(args).popen();
            popen.waitFor();
            String cid =
                    popen.verifyOrDieWith("Failed to run selenium container").trim();

            new ProcessBuilder(Docker.cmd("logs", "-f", cid).toCommandArray())
                    .redirectErrorStream(true)
                    .redirectOutput(log.toFile())
                    .start();

            Closeable cleanContainer = new Closeable() {
                @Override
                public void close() {
                    try {
                        Docker.cmd("kill", cid).popen().verifyOrDieWith("Failed to kill " + cid);
                        Docker.cmd("rm", cid).popen().verifyOrDieWith("Failed to rm " + cid);
                    } catch (IOException | InterruptedException e) {
                        throw new Error("Failed removing container", e);
                    }
                }

                @Override
                public String toString() {
                    return "Kill and remove selenium container";
                }
            };
            Thread.sleep(3000); // Give the container and selenium some time to spawn

            try {
                WebDriver driver = RemoteWebDriver.builder()
                        .address("http://127.0.0.1:" + controlPort + "/wd/hub")
                        .addAlternative(capabilities)
                        .build();
                cleaner.addTask(cleanContainer);
                return driver;
            } catch (RuntimeException e) {
                cleanContainer.close();
                throw e;
            } catch (Throwable e) {
                cleanContainer.close();
                throw new Error(e);
            }
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private Proxy createSeleniumProxy(String testName) throws UnknownHostException {
        // if we are running maven locally but the browser elsewhere (e.g. docker) using the "127.0.0.1"
        // address will not work for the browser
        String name = System.getenv("SELENIUM_PROXY_HOSTNAME");
        InetAddress proxyAddr;
        if (name != null) {
            proxyAddr = InetAddress.getByName(name);
        } else {
            // bind to the loopback to prevent exposing the proxy to the world.
            proxyAddr = InetAddress.getLoopbackAddress();
        }
        BrowserUpProxy proxy = HarRecorder.getProxy(proxyAddr, testName);
        return ClientUtil.createSeleniumProxy(proxy, proxyAddr);
    }

    private void setDriverPropertyIfMissing(final String driverCommand, final String property) {
        if (System.getProperty(property) != null) {
            return;
        }
        String executable = locateDriver(driverCommand);
        if (StringUtils.isNotBlank(executable)) {
            System.setProperty(property, executable);
        } else {
            LOGGER.warning("Unable to locate " + driverCommand);
        }
    }

    private String locateDriver(final String name) {
        String command = SystemUtils.IS_OS_WINDOWS ? "where" : "which";
        try (ProcessInputStream pis = new CommandBuilder(command, name).popen()) {
            return pis.asText().trim();
        } catch (IOException | InterruptedException exception) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Get display number to run browser on.
     */
    public static @CheckForNull String getBrowserDisplay() {
        return System.getenv("DISPLAY");
    }

    /**
     * Creates a {@link WebDriver} for each test, then make sure to clean it up at the end.
     */
    @Provides
    @TestScope
    public WebDriver createWebDriver(TestCleaner cleaner, TestName testName, ElasticTime time) throws IOException {
        WebDriver base = createWebDriver(cleaner, testName);

        // Make sure the window has minimal resolution set, even when out of the visible screen.
        // Note - not maximizing here any more because that doesn't do anything.
        Dimension oldSize = base.manage().window().getSize();
        if (oldSize.height < 1090 || oldSize.width < 1680) {
            base.manage().window().setSize(new Dimension(1680, 1090));
        }
        Scroller scroller = new Scroller(base);
        final EventFiringDecorator<WebDriver> decorator = new EventFiringDecorator<>(scroller);
        WebDriver d = decorator.decorate(base);

        try {
            d.manage().timeouts().pageLoadTimeout(Duration.ofMillis(time.seconds(PAGE_LOAD_TIMEOUT)));
            d.manage().timeouts().implicitlyWait(Duration.ofMillis(time.seconds(IMPLICIT_WAIT_TIMEOUT)));
        } catch (UnsupportedCommandException e) {
            // sauce labs RemoteWebDriver doesn't support this
            LOGGER.info(base + " doesn't support page load timeout");
        }
        String testNameStr = testName.get();
        cleaner.addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable error = null;

                try {
                    d.quit();
                } catch (Throwable t) {
                    error = t;
                }

                /*
                 * Quitting the driver writes out the recording, so only after this is done can we process the
                 * recording. Unfortunately, this is after FailureDiagnostics has finished its job, so we have to
                 * duplicate some of its logic here.
                 */
                try {
                    processRecording();
                } catch (Throwable t) {
                    if (error == null) {
                        error = t;
                    } else {
                        error.addSuppressed(t);
                    }
                }

                if (error != null) {
                    throw error;
                }
            }

            private void processRecording() throws IOException, InterruptedException {
                String videoFolder = System.getenv("VIDEO_FOLDER");
                if (videoFolder == null) {
                    return;
                }

                Path src = Paths.get(videoFolder).resolve(normalize(testNameStr));
                if (!Files.exists(src)) {
                    return;
                }

                /*
                 * FailureDiagnostics will have deleted this directory if the test passed, which is our clue whether to
                 * retain or delete the video in the common case where we only retain the video if the test failed. When
                 * the test passed and we want to retain all videos, the directory won't exist, so we create it.
                 */
                Path diagnostics = Paths.get("target").resolve("diagnostics").resolve(testNameStr);
                if (TestRecorderRule.saveAllExecutions()) {
                    Files.createDirectories(diagnostics);
                }
                if (Files.isDirectory(diagnostics)) {
                    waitUntilLastFramesAreRecorded(src);
                    Path dest = diagnostics.resolve("ui-recording.mp4");
                    Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    System.out.printf("[[ATTACHMENT|%s]]%n", dest.toAbsolutePath());
                } else {
                    Files.delete(src);
                }
            }

            /**
             * Normalize the video file name
             */
            private static String normalize(String testName) {
                // https://github.com/SeleniumHQ/docker-selenium/blob/cabae69a6e7542b2527072d677020a34fb1fce70/Video/video_nodeQuery.sh#L43-L44
                String normalized = testName.replace(' ', '_');
                normalized = normalized.replaceAll("[^a-zA-Z0-9_-]", "");
                normalized = normalized.substring(0, Math.min(251, normalized.length()));
                normalized += ".mp4";
                return normalized;
            }

            /**
             * Wait up to 1 second for the video container to finish writing out the last few frames of video.
             */
            private static void waitUntilLastFramesAreRecorded(Path src) throws IOException, InterruptedException {
                long lastSize = Files.size(src);
                long lastChangeTime = System.nanoTime();
                while (true) {
                    Thread.sleep(100);
                    long currentSize = Files.size(src);
                    long currentTime = System.nanoTime();
                    if (currentSize != lastSize) {
                        lastSize = currentSize;
                        lastChangeTime = currentTime;
                    } else if (currentTime - lastChangeTime >= 1_000_000_000L) {
                        break;
                    }
                }
            }

            @Override
            public String toString() {
                return "Close WebDriver after test";
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
    @Provides
    @TestScope
    public JenkinsController createController(Injector injector, ExtensionList<JenkinsControllerFactory> factories)
            throws IOException {
        String type = System.getenv("type"); // this is lower case for backward compatibility
        if (type == null) {
            type = System.getenv("TYPE");
        }
        if (type == null) {
            File socket = getSocket();
            if (socket.exists() && !JenkinsControllerPoolProcess.MAIN) {
                LOGGER.info("Found pooled jenkins controller listening on socket " + socket.getAbsolutePath());
                return new PooledJenkinsController(injector, socket);
            } else {
                LOGGER.warning("No pooled jenkins controller listening on socket " + socket.getAbsolutePath());
                type = "winstone";
            }
        }

        for (JenkinsControllerFactory f : factories) {
            if (f.getId().equalsIgnoreCase(type)) {
                final JenkinsController c = f.create();
                c.postConstruct(injector);
                return c;
            }
        }

        throw new AssertionError("Invalid controller type: " + type);
    }

    @Provides
    @TestScope
    public Jenkins createJenkins(Injector injector, JenkinsController controller) {
        if (!controller.isRunning()) {
            return null;
        }
        return new Jenkins(injector, controller);
    }

    /**
     * Returns whether Jenkins should be quite and should not report any logging information.
     *
     * @return {@code true} if Jenkins should be quite during the tests
     */
    @Provides
    @Named("quite")
    public boolean getQuite() {
        return System.getProperty("quite") != null;
    }

    /**
     * directory on the computer where this code is running that points to a directory
     * where test code can place log files, cache files, etc.
     * Note that this directory might not exist on the Jenkins master, since it can be
     * running on a separate computer.
     */
    @Provides
    @Named("WORKSPACE")
    public String getWorkspace() {
        return new File(System.getProperty("user.dir"), "target").getPath();
    }

    /**
     * Name of the socket file used to communicate between jut-server and JUnit.
     * See {@code docs/PRELAUNCH.md}
     *
     * @return the name of the socket
     * @see JenkinsControllerPoolProcess
     */
    @Provides
    @Named("socket")
    public File getSocket() {
        String socket = System.getenv("JUT_SOCKET");
        if (StringUtils.isNotBlank(socket)) {
            return new File(socket);
        }
        return new File(System.getProperty("user.home"), "jenkins.sock");
    }

    /**
     * Get the file with Jenkins to run.
     * <p>
     * The file will exist on machine where tests run.
     */
    @Provides
    @Named("jenkins.war")
    public File getJenkinsWar(RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession) {
        try {
            return IOUtil.firstExisting(false, System.getenv("JENKINS_WAR"));
        } catch (IOException ignored) {
        }

        String version = System.getenv("JENKINS_VERSION");
        if (version != null && !version.isEmpty()) {
            ArtifactResolverUtil resolverUtil = new ArtifactResolverUtil(repositorySystem, repositorySystemSession);
            ArtifactResult resolvedArtifact =
                    resolverUtil.resolve(new DefaultArtifact("org.jenkins-ci.main", "jenkins-war", "war", version));
            return resolvedArtifact.getArtifact().getFile();
        }

        // TODO add support for 'lts', 'lts-rc', 'latest' and 'latest-rc'

        try {
            // Lowest priority of all
            return IOUtil.firstExisting(
                    false, System.getenv("JENKINS_WAR"), getWorkspace() + "/jenkins.war", "./jenkins.war");
        } catch (IOException ex) {
            throw new Error("Could not find jenkins.war, use JENKINS_WAR or JENKINS_VERSION to specify it.", ex);
        }
    }

    /**
     *  Provides a mechanism to create a report on which plugins were used
     *  during the test execution
     *
     *  @return An ExercisedPluginReporter based on env var EXERCISEDPLUGINREPORTER
     */
    @Provides
    @Named("ExercisedPluginReporter")
    public ExercisedPluginsReporter createExercisedPluginReporter() {
        String reporter = System.getenv("EXERCISEDPLUGINREPORTER");
        if (reporter == null) {
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
                throw new AssertionError("Unrecognized Exercised Plugin Report type: " + reporter);
        }
    }
}
