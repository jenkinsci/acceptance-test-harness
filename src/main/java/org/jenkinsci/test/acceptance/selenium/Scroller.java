package org.jenkinsci.test.acceptance.selenium;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.AddInterceptParameters;
import org.openqa.selenium.bidi.network.BytesValue;
import org.openqa.selenium.bidi.network.ContinueRequestParameters;
import org.openqa.selenium.bidi.network.Header;
import org.openqa.selenium.bidi.network.InterceptPhase;
import org.openqa.selenium.bidi.network.ProvideResponseParameters;
import org.openqa.selenium.bidi.network.RequestData;
import org.openqa.selenium.bidi.network.UrlPattern;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * Automatically scrolls the element into view.
 *
 * <p>
 * Especially in the configuration page, the floating DIVs at the top and
 * the bottom of the pages can interfere with WebDriver trying to click
 * the elements underneath it.
 * <p>
 * At least on Chrome (and possibly in other browsers), trying to interact
 * with an element when it's below another element causes the following error:
 *
 * <pre>
 * {@code
 * Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 227.711 sec <<< FAILURE! - in plugins.AntPluginTest
 * autoInstallAnt(plugins.AntPluginTest)  Time elapsed: 73.932 sec  <<< ERROR!
 * org.openqa.selenium.WebDriverException: unknown error: Element is not clickable at point (506, 967). Other element would receive the click: <div class="bottom-sticker-inner">...</div>
 *   (Session info: chrome=34.0.1847.116)
 *   (Driver info: chromedriver=2.10.267518,platform=Linux 3.13.0-24-generic x86_64) (WARNING: The server did not provide any stacktrace information)
 * Command duration or timeout: 61 milliseconds
 * Build info: version: '2.40.0', revision: '4c5c0568b004f67810ee41c459549aa4b09c651e', time: '2014-02-19 11:13:01'
 * System info: host: 'jglick-t520', ip: '127.0.1.1', os.name: 'Linux', os.arch: 'amd64', os.version: '3.13.0-24-generic', java.version: '1.7.0_55'
 * Session ID: 412a7b3ab0eb823da2cddd156e8d122c
 * Driver info: org.openqa.selenium.chrome.ChromeDriver
 * Capabilities [{platform=LINUX, acceptSslCerts=true, javascriptEnabled=true, browserName=chrome, chrome={userDataDir=/tmp/.com.google.Chrome.3Xt0hp}, rotatable=false, locationContextEnabled=true, version=34.0.1847.116, takesHeapSnapshot=true, cssSelectorsEnabled=true, databaseEnabled=false, handlesAlerts=true, browserConnectionEnabled=false, nativeEvents=true, webStorageEnabled=true, applicationCacheEnabled=false, takesScreenshot=true}]
 * 	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
 * 	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
 * 	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
 * 	at java.lang.reflect.Constructor.newInstance(Constructor.java:526)
 * 	at org.openqa.selenium.remote.ErrorHandler.createThrowable(ErrorHandler.java:193)
 * 	at org.openqa.selenium.remote.ErrorHandler.throwIfResponseFailed(ErrorHandler.java:145)
 * 	at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:573)
 * 	at org.openqa.selenium.remote.RemoteWebElement.execute(RemoteWebElement.java:268)
 * 	at org.openqa.selenium.remote.RemoteWebElement.click(RemoteWebElement.java:79)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
 * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 * 	at java.lang.reflect.Method.invoke(Method.java:606)
 * 	at org.openqa.selenium.support.events.EventFiringWebDriver$EventFiringWebElement$1.invoke(EventFiringWebDriver.java:331)
 * 	at com.sun.proxy.$Proxy33.click(Unknown Source)
 * 	at org.openqa.selenium.support.events.EventFiringWebDriver$EventFiringWebElement.click(EventFiringWebDriver.java:344)
 * 	at org.jenkinsci.test.acceptance.po.CapybaraPortingLayer.clickButton(CapybaraPortingLayer.java:66)
 * 	at org.jenkinsci.test.acceptance.po.JenkinsConfig.addTool(JenkinsConfig.java:35)
 * 	at org.jenkinsci.test.acceptance.plugins.ant.AntInstallation.install(AntInstallation.java:37)
 * 	at plugins.AntPluginTest.autoInstallAnt(AntPluginTest.java:76)
 * 	}
 * </pre>
 *
 * <p>
 * This work around simply tries to scroll the element into a view before we interact with this.
 * Originally developed in Ruby version of selenium-tests in {@code lib/jenkins/capybara.rb}.
 *
 * @author ogondza
 * @author Kohsuke Kawaguchi
 */
public class Scroller implements WebDriverListener {
    private final Logger LOGGER = Logger.getLogger(Scroller.class.getName());

    private final String disableStickyElementsJs;
    private final WebDriver driver;

    public Scroller(WebDriver driver) {
        this.driver = driver;
        try (InputStream sticky = getClass().getResourceAsStream("disable-sticky-elements.js")) {
            disableStickyElementsJs = new String(sticky.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Error("Failed to load the JavaScript file", e);
        }

        Network network = new Network(driver);
        AddInterceptParameters p = new AddInterceptParameters(InterceptPhase.BEFORE_REQUEST_SENT);
        /**
         * this currently does not work
         * https://seleniumhq.slack.com/archives/C0ABCS03F/p1779116208958639
         * https://github.com/w3c/webdriver-bidi/pull/1106
         */
        UrlPattern up = new UrlPattern();
        up.protocol("http");
        // up.pathname("/\\*context/static/\\*/{\\*:filename\\}.scss");
        // up.pathname("\\(.\\*.css\\)");
        p.urlPattern(up);

        // https://urlpattern.spec.whatwg.org/
        // p.urlStringPattern("http\\{s\\}?://domain.com/foo.scss");

        String id = network.addIntercept(p);
        network.onBeforeRequestSent(request -> {
            final String responseId = request.getRequest().getRequestId();
            final RequestData requestData = request.getRequest();
            // if (responseDetails.getIntercepts().contains(id)) {
            String requestUrl = requestData.getUrl();
            if (requestUrl.endsWith(".css") && requestUrl.contains("/static/") && requestUrl.startsWith("http")) {
                // smells like a static jenkins css file...
                try {
                    // extract a cookie for authentication
                    String cookie = extractCookieHeader(requestData);

                    String css;
                    try (HttpClient client = HttpClient.newHttpClient()) {
                        HttpRequest clientRequest = HttpRequest.newBuilder()
                                .uri(URI.create(requestUrl))
                                .header("Cookie", cookie)
                                .build();
                        HttpResponse<String> clientResponse =
                                client.send(clientRequest, BodyHandlers.ofString(StandardCharsets.UTF_8));
                        css = clientResponse.body();
                    }

                    String patchedCSS = removeStickyPositioning(css);
                    LOGGER.warning(() -> "replaced sticky ***before:***\n" + css + "\n***after:***\n" + patchedCSS);

                    ProvideResponseParameters responseParams = new ProvideResponseParameters(responseId);
                    responseParams.body(new BytesValue(BytesValue.Type.STRING, patchedCSS));
                    // skip headers...
                    /*
                    List<Header> headers = new ArrayList<>();
                    for (Map.Entry<String, List<String>> entry :
                            con.getHeaderFields().entrySet()) {
                        if ("Content-Length".equalsIgnoreCase(entry.getKey())) {
                            // we are manipluating the body ignore the size.
                            continue;
                        }
                        for (String v : entry.getValue()) {
                            if (entry.getKey() == null) {
                                LOGGER.warning(() -> "attempted to add null header with value '" + v + "'");
                                continue;
                            }
                            LOGGER.warning(() -> "adding header '" + entry.getKey() + "' with value '" + v + "'");
                            headers.add(new Header(entry.getKey(), new BytesValue(BytesValue.Type.STRING, v)));
                        }
                    }
                    responseParams.headers(headers);
                    */
                    // TODO add expiry / last modified for caching...?
                    responseParams.statusCode(200);
                    network.provideResponse(responseParams);
                    // network.continueResponse(new ContinueResponseParameters(responseId).);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not recieve CSS from Jenkins at URL:" + requestUrl, e);
                } catch (InterruptedException e) {
                    throw new UncheckedIOException(
                            "Could not recieve CSS from Jenkins at URL: " + requestUrl, new IOException("cause", e));
                }

            } else {
                try {
                    network.continueRequest(new ContinueRequestParameters(responseId));
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void afterGet(WebDriver driver, String url) {
        // LOGGER.warning(() -> "afterGet, driver.getCurrentUrl()=" + driver.getCurrentUrl() + " url="+url);
        disableStickyElementsIgnoringDialogs();
    }

    @Override
    public void afterTo(Navigation navigation, String url) {
        // LOGGER.warning(() -> "afterTo, navigation=" + navigation + " url="+url);
        disableStickyElementsIgnoringDialogs();
    }

    @Override
    public void afterTo(Navigation navigation, URL url) {
        // LOGGER.warning(() -> "afterTo, navigation=" + driver.getCurrentUrl() + " url="+url);
        disableStickyElementsIgnoringDialogs();
    }

    private String preClickURL;

    @Override
    public void beforeClick(WebElement element) {
        preClickURL = driver.getCurrentUrl();
        WebDriverListener.super.beforeClick(element);
    }

    @Override
    public void afterClick(WebElement element) {
        if (!Objects.equals(preClickURL, driver.getCurrentUrl())) {
            // we have performed navigation via a click
            disableStickyElements();
        }
        preClickURL = null;
    }

    private void disableStickyElementsIgnoringDialogs() {
        try {
            // if there's an alert we can't run JavaScript
            // if we catch the exception from running JavaScript then FormValidationTest hangs
            // not the nicest hack, but it works
            driver.switchTo().alert();
        } catch (NoAlertPresentException e) {
            disableStickyElements();
        }
    }

    /**
     * Sometimes sticky elements (elements that are fixed in position on the page, such as the bottom app bar),
     * appear on top of other elements, making those elements inaccessible. This method removes the sticky
     * nature of these elements meaning that they'll no longer appear on top of other elements.
     */
    public void disableStickyElements() {
        /*
        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        try {
            executor.executeScript(disableStickyElementsJs);
        } catch (UnhandledAlertException unexpected) {
            // if we're in the process of navigating but an alert is in the way we can't run JS
            LOGGER.log(
                    Level.WARNING,
                    "Failed to disable stick elements, letting the test to continue anyways, but \"Element is not clickable\" error will likely be thrown",
                    unexpected);
        }
        */
    }

    private String extractCookieHeader(RequestData requestData) {
        for (Header h : requestData.getHeaders()) {
            if ("Cookie".equalsIgnoreCase(h.getName())) {
                BytesValue v = h.getValue();

                switch (v.getType()) {
                    case BytesValue.Type.STRING:
                        return v.getValue();
                    default:
                        throw new IllegalStateException("expected a String header for cookie, but got: " + v.getType());
                }
            }
        }
        return null;
    }

    private String removeStickyPositioning(String css) {
        return css.replaceAll("position:\\s*sticky", "position: relative");
    }
}
